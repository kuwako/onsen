package com.example.kuwako.onsen.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.toolbox.Volley;
import com.example.kuwako.onsen.R;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

// 検索画面
public class MainActivity extends BaseAppCompatActivity {

    private Button prefSearchBtn;
    private Button mapSearchBtn;
    private Spinner prefSpinner;
    private String prefNameList[] = {"東京", "大阪", "愛知"};
    private ArrayAdapter<String> adapter;
    private RequestQueue mRequestQueue;

    private String prefUri = "http://loco-partners.heteml.jp/u/prefectures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 都道府県検索ボタン
        prefSearchBtn = (Button) findViewById(R.id.prefSearchBtn);
        prefSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "都道府県検索");
            }
        });

        // 現在地検索ボタン
        mapSearchBtn = (Button) findViewById(R.id.mapSearchBtn);
        mapSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "マップ検索");
            }
        });

        // スピナーに登録
        adapter = new ArrayAdapter<String>
                (this, R.layout.support_simple_spinner_dropdown_item, prefNameList);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        prefSpinner = (Spinner) findViewById(R.id.prefSpinner);

        // TODO 都道府県取得
        getPrefData();

        prefSpinner.setAdapter(adapter);

        // スピナのリスナ登録
        prefSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();

                Log.d(LOG_TAG, item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

    // 都道府県取得
    private void getPrefData() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        mRequestQueue.add(new JsonObjectRequest(Request.Method.GET, prefUri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // 通信成功時の処理
                Log.d(LOG_TAG, "通信成功");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗時
                Log.d(LOG_TAG, error.toString());
            }
        }));
    }
}
