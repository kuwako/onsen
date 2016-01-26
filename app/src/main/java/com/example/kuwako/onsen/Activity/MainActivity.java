package com.example.kuwako.onsen.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.kuwako.onsen.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import static android.location.LocationManager.*;

// 検索画面
public class MainActivity extends BaseAppCompatActivity implements LocationListener {

    private ArrayAdapter<String> mAdapter;
    private RequestQueue mRequestQueue;
    private int mPrefId = 1;
    private LocationManager mLocationManager = null;
    private String mProvider = "";
    private Location mLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO TOPのActivityに戻るボタン必要か相談。要らない気がするので、一旦コメントアウト
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsFlg = mLocationManager.isProviderEnabled(GPS_PROVIDER);
        Log.d(LOG_TAG, gpsFlg ? "GPS OK" : "GPS NG");

        // 都道府県検索ボタン
        Button prefSearchBtn = (Button) findViewById(R.id.prefSearchBtn);
        prefSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "都道府県検索");
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra(getString(R.string.pref_id), mPrefId);
                startActivity(intent);
            }
        });

        // 現在地検索ボタン
        Button mapSearchBtn = (Button) findViewById(R.id.mapSearchBtn);
        // TODO GPSオフの時の処理
        mapSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "マップ検索");
                // GPSセンサが利用可能か
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // GPS利用不可ならば利用可能に設定してもらう
                    Toast.makeText(getApplicationContext(), "GPSをONにしてください", Toast.LENGTH_LONG).show();
                    Intent callGPSSettingIntent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(callGPSSettingIntent);
                } else {
                    // GPSセンサ利用可能なら
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    mLocation = mLocationManager.getLastKnownLocation(mProvider);

                    if (mLocation != null) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        intent.putExtra("mapSearch", true);
                        intent.putExtra("latitude", mLocation.getLatitude());
                        intent.putExtra("longitude", mLocation.getLongitude());

                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "現在地取得中です。しばらく経ってからもう一度クリックしてください", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

        // スピナーに登録
        mAdapter = new ArrayAdapter<String>
                (this, R.layout.support_simple_spinner_dropdown_item);

        // 外部APIから都道府県をjsonで取得してadapterにset
        getPrefData();

        mAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        Spinner prefSpinner = (Spinner) findViewById(R.id.prefSpinner);

        prefSpinner.setAdapter(mAdapter);

        // スピナのリスナ登録
        prefSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;

                // 取得できる値が0からスタートなので +1
                mPrefId = spinner.getSelectedItemPosition() + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 現在地取得
        Criteria criteria = new Criteria();
        // 精度
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mProvider = mLocationManager.getBestProvider(criteria, true);

        Log.d(LOG_TAG, "provider = " + mProvider);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                }
            }
            mLocationManager.requestLocationUpdates(mProvider, 0, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
            }
        }
        mLocationManager.removeUpdates(this);
    }

    // 都道府県取得
    private void getPrefData() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        String prefUri = "http://loco-partners.heteml.jp/u/prefectures";
        mRequestQueue.add(new JsonObjectRequest(Request.Method.GET, prefUri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject prefJson) {
                // 通信成功時の処理
                Log.d(LOG_TAG, "通信成功");
                // JSONのパース
                try {
                    // 都道府県情報をadapterに追加
                    for (int i = 1; i <= prefJson.length(); i++) {
                        String prefName = prefJson.getString(String.valueOf(i));
                        mAdapter.add(prefName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗時
                Log.d(LOG_TAG, error.toString());
            }
        }));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "location is changed.");
        Log.d(LOG_TAG, "lat: " + location.getLatitude() + " log: " + location.getLongitude());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
            }
        }
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
