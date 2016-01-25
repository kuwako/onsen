package com.example.kuwako.onsen.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kuwako on 2016/01/20.
 */
public class BaseAppCompatActivity extends AppCompatActivity{

    public String LOG_TAG = "onsenLog";
    public String BASE_URL = "http://loco-partners.heteml.jp/u/onsens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
