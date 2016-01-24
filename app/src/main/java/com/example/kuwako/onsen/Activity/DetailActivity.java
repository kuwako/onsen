package com.example.kuwako.onsen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.kuwako.onsen.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private JSONObject onsenJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        // 温泉情報を取得
        String onsenJsonStr = intent.getStringExtra("onsen");
        // 温泉情報をJsonに変換
        try {
            onsenJson = new JSONObject(onsenJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setOnsenInfo();
    }

    private void setOnsenInfo() {
        TextView name = (TextView) findViewById(R.id.name);
        TextView kana = (TextView) findViewById(R.id.kana);
        TextView address = (TextView) findViewById(R.id.address);
        TextView tel = (TextView) findViewById(R.id.tel);
        TextView price = (TextView) findViewById(R.id.price);
        TextView close_day = (TextView) findViewById(R.id.close_day);
        TextView open_hour = (TextView) findViewById(R.id.open_hour);
        TextView spring_quality = (TextView) findViewById(R.id.spring_quality);

        try {
            name.setText(onsenJson.getString("name"));
            kana.setText(onsenJson.getString("kana"));
            address.setText(onsenJson.getString("address"));
            tel.setText(onsenJson.getString("tel"));
            price.setText(onsenJson.getString("price"));
            close_day.setText(onsenJson.getString("close_day"));
            open_hour.setText(onsenJson.getString("open_hour"));
            spring_quality.setText(onsenJson.getString("spring_quality"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
