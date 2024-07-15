package com.omsi.softaptest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.omsi.softap.SoftApManager;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoftApManager.startSoftAp(MainActivity.this, "ANDR_01_D", "123456789", false);
            }
        });

        findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoftApManager.stopSoftAp(MainActivity.this);
            }
        });

        findViewById(R.id.check_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean res = SoftApManager.isSoftApActive(MainActivity.this);
                Log.d(TAG, "SoftAp is "+ (res? "active": "inactive"));
            }
        });
    }

}