package com.example.sleepalarm;

import android.content.ContentProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends AppCompatActivity {

    private final int DISPLAY_TIME=2000;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        preferences=getSharedPreferences("config",MODE_PRIVATE);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editor=preferences.edit();
                editor.putBoolean("intro",false);
                editor.commit();
                Intent mainit=new Intent(LaunchActivity.this,MainActivity.class);
                LaunchActivity.this.startActivity(mainit);
                LaunchActivity.this.finish();
            }
        },DISPLAY_TIME);

    }
}
