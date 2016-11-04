package com.example.martinhuang.mapgps;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

public class SearchLocationActivity extends AppCompatActivity {

    public static final String CENTER_X = "CENTER_X";
    public static final String CENTER_Y = "CENTER_Y";

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        toolbar = (Toolbar)findViewById(R.id.search_toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.colorBlue));
        changeStatusBarColor(ContextCompat.getColor(this,R.color.colorBlue));

        initToolbar();

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @TargetApi(21)
    private void changeStatusBarColor(int color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            this.getWindow().setStatusBarColor(color);
        }
    }
}
