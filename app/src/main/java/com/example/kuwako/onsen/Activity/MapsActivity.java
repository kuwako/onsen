package com.example.kuwako.onsen.Activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.kuwako.onsen.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng position;

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
            position = prefSearch(prefId);
        }

        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(position).title("Let's 温泉"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    // 都道府県検索
    private LatLng prefSearch(int prefId) {
        // prefIdから周辺の温泉情報取得

        // JSONパース

        // forで回してマーカー追加
        // マーカーには温泉名とDetailへのリンク
    }
}
