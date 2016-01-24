package com.example.kuwako.onsen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.example.kuwako.onsen.R;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends BaseAppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng position;
    private RequestQueue mRequestQueue;
    private JSONArray onsenListJson;

    // ピンが出すぎるのもわかりづらいので、とりあえずlimit指定。
    private String onsenLimit = "30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent;

        intent = getIntent();
        int prefId = intent.getIntExtra(getString(R.string.pref_id), 0);

        if (prefId != 0) {
            // 外部APIを叩いてpositionと温泉のJSONObjectセット
            prefSearch(prefId);
        }

        // Add a marker in Sydney and move the camera
//        mMap.addMarker(new MarkerOptions().position(position).title("Let's 温泉"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    // 都道府県検索
    private void prefSearch(int prefId) {
        // 都道府県検索用のURL
        String prefUri = "http://loco-partners.heteml.jp/u/onsens?prefecture=" + String.valueOf(prefId) + "&limit=" + onsenLimit;

        // 外部APにアクセスして温泉情報取得
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        } else {
            mRequestQueue.start();
        }

        mRequestQueue.add(new JsonArrayRequest(prefUri, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray prefOnsenListJson) {
                // 通信成功時の処理
                Log.d(LOG_TAG, "通信成功");
                // JSONのパース
                try {
                    // 都道府県用は中心点が決まっていないので、とりあえず検索で引っかかった1件目を中心に設定。
                    position = new LatLng(
                            Double.parseDouble(prefOnsenListJson.getJSONObject(0).getJSONObject("Onsen").getString("latitude")),
                            Double.parseDouble(prefOnsenListJson.getJSONObject(0).getJSONObject("Onsen").getString("longitude"))
                    );

                    // JSONをセット
                    onsenListJson = prefOnsenListJson;
                    mMap.addMarker(new MarkerOptions().position(position).title("Let's 温泉"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
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
    protected void onStart() {
        super.onStart();

//        mRequestQueue.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

//        mRequestQueue.stop();
    }
}
