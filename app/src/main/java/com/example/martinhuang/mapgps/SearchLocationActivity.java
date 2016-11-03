package com.example.martinhuang.mapgps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SearchLocationActivity extends AppCompatActivity {

    public static final String CENTER_X = "CENTER_X";
    public static final String CENTER_Y = "CENTER_Y";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
    }
}
