package com.example.kuwako.onsen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.kuwako.onsen.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends BaseAppCompatActivity {
    private JSONObject mOnsenJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        // 温泉情報を取得
        String onsenJsonStr = intent.getStringExtra("onsen");
        // 温泉情報をJsonに変換
        try {
            mOnsenJson = new JSONObject(onsenJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 温泉情報をView側にセット
        setOnsenInfo();
    }

    // 温泉情報をView側にセット
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
            name.setText(mOnsenJson.getString("name"));
            kana.setText(mOnsenJson.getString("kana"));
            address.setText(mOnsenJson.getString("address"));
            tel.setText(mOnsenJson.getString("tel"));
            price.setText(mOnsenJson.getString("price"));
            close_day.setText(mOnsenJson.getString("close_day"));
            open_hour.setText(mOnsenJson.getString("open_hour"));
            spring_quality.setText(mOnsenJson.getString("spring_quality"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
