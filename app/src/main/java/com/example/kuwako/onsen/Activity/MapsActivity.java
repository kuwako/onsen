package com.example.kuwako.onsen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.example.kuwako.onsen.R;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends BaseAppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mPosition;
    private RequestQueue mRequestQueue;
    private JSONArray mOnsenListJson;
    private final Double DEFAULT_LAT = 35.0;
    private final Double DEFAULT_LON = 135.0;

    // Mapの表示範囲レベル都道府県一個表示できるかできないかぐらいのサイズ
    private final float ZOOM_LEVEL = 9.0f;

    // ピンが出すぎるのもわかりづらいので、とりあえずlimit指定。
    private String onsenLimit = "30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
        boolean isMapSearch = intent.getBooleanExtra("mapSearch", false);

        if (prefId != 0) {
            // 外部APIを叩いてpositionと温泉のJSONObjectセット
            prefSearch(prefId);
        } else if (isMapSearch) {
            // 現在地検索だった場合
            // デフォ値はとりあえず日本標準時にしておく
            mapSearch(intent.getDoubleExtra("latitude", DEFAULT_LAT),
                    intent.getDoubleExtra("longitude", DEFAULT_LON));
        }
    }

    // 現在地検索
    private void mapSearch(Double lat, Double lon) {
        // mapの中心点設定
        mPosition = new LatLng(lat, lon);

        // 与えられた外部APIでは、緯度経度による最小外接円内検索が取得できるため、二つの緯度経度が必要。
        // lat、lonを引数±0.5して現在地を中心とする直径が経度1度分の円内の温泉を検索
        // NOTE: 経度1度分の円でだいたい群馬がすっぽり入るぐらいっぽいのでちょうどいいはず
        Double latlonDiff = 0.5;
        String lonEast = String.valueOf(lon + latlonDiff);
        String lonWest = String.valueOf(lon - latlonDiff);
        String latEast = String.valueOf(lat + latlonDiff);
        String latWest = String.valueOf(lat - latlonDiff);

        // 現在地取得用外部APIをURL生成
        String mapUri = BASE_URL + "/geometric_search?point[]=" +
                lonEast + "," + latEast + "&point[]=" + lonWest + "," + latWest + "&limit=" + onsenLimit;

        // 外部APにアクセスして温泉情報取得
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        } else {
            mRequestQueue.start();
        }
        mRequestQueue.add(new JsonArrayRequest(mapUri, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray mapOnsenListJson) {
                // 通信成功時の処理
                Log.d(LOG_TAG, "通信成功");

                // JSONをセット
                mOnsenListJson = mapOnsenListJson;
                // ピンを立てる
                setPinsOnMap();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗時
                Log.d(LOG_TAG, error.toString());
            }
        }));
    }

    // 都道府県検索
    private void prefSearch(int prefId) {
        // 都道府県検索用のURL
        String prefUri = BASE_URL + "?prefecture=" + String.valueOf(prefId) + "&limit=" + onsenLimit;

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
                    mPosition = new LatLng(
                            Double.parseDouble(prefOnsenListJson.getJSONObject(0).getJSONObject("Onsen").getString("latitude")),
                            Double.parseDouble(prefOnsenListJson.getJSONObject(0).getJSONObject("Onsen").getString("longitude"))
                    );

                    // JSONをセット
                    mOnsenListJson = prefOnsenListJson;

                    setPinsOnMap();
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

    // GoogleMap上に温泉のpinを表示
    private void setPinsOnMap() {
        // ズームレベルを指定して表示場所移動
        CameraPosition pos = new CameraPosition(mPosition, ZOOM_LEVEL, 0.0f, 0.0f);
        CameraUpdate camera = CameraUpdateFactory.newCameraPosition(pos);
        mMap.moveCamera(camera);

        for (int i = 0; i < mOnsenListJson.length(); i++) {
            try {
                final JSONObject onsenJson = mOnsenListJson.getJSONObject(i).getJSONObject("Onsen");

                // 温泉の緯度経度
                LatLng onsenLatLng = new LatLng(
                        Double.parseDouble(onsenJson.getString("latitude")),
                        Double.parseDouble(onsenJson.getString("longitude")));

                // ピンの詳細設定
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(onsenJson.getString("name"));
                markerOptions.position(onsenLatLng);
                markerOptions.snippet(onsenJson.getString("address"));

                // ピンを立てる
                mMap.addMarker(markerOptions);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        View view = getLayoutInflater().inflate(R.layout.custome_info_window, null);
                        // タイトル
                        TextView title = (TextView) view.findViewById(R.id.marker_title);
                        title.setText(marker.getTitle());
                        // 詳細
                        TextView snippet = (TextView) view.findViewById(R.id.marker_description);
                        snippet.setText(marker.getSnippet());

                        /* TODO
                         *  infoWindowにボタンやチェックボックスをつけるのはGoogle的に想定されていなくて難易度高いので後回し。
                         *  詳細ボタンではなくinfoWindow自体にクリック判定をつける。
                         */
                        // 詳細ボタン
                        /*
                        Button btn = (Button) view.findViewById(R.id.marker_btn);
                        btn.setOnClickListener(new View.OnClickListener() {
                            // 詳細ボタンクリック時の処理
                            @Override
                            public void onClick(View v) {
                                Log.d(LOG_TAG, "marker click");
                                Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                                intent.putExtra("onsen", String.valueOf(onsenJson));
                                startActivity(intent);
                            }
                        });
                        */

                        return view;
                    }


                    @Override
                    public View getInfoContents(Marker marker) {
                        return null;
                    }
                });

                // infoWindow内にボタンを設置するのが上述の理由で難易度高そうなのでとりあえずこっちで実装
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    // infoWindowクリック時に温泉詳細に飛ぶ。
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Log.d(LOG_TAG, "marker click");
                        Intent intent = new Intent(MapsActivity.this, DetailActivity.class);

                        // 選択された温泉のJSONデータを取得してintentに挿入
                        for (int i = 0; i < mOnsenListJson.length(); i++) {
                            try {
                                JSONObject onsenJson = mOnsenListJson.getJSONObject(i).getJSONObject("Onsen");
                                if (onsenJson.getString("name").equals(marker.getTitle())) {
                                    // TODO もっと丁寧にJSON渡す方法ありそう
                                    intent.putExtra("onsen", String.valueOf(onsenJson));

                                    startActivity(intent);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mRequestQueue != null) {
            mRequestQueue.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mRequestQueue != null) {
            mRequestQueue.stop();
        }
    }
}
