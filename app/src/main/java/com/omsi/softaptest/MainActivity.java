package com.omsi.softaptest;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.omsi.softap.SoftApManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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

        findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener()   {
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


        findViewById(R.id.enable_adb_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG, "tcp port");
                    Process  p = Runtime.getRuntime().exec("setprop service.adb.tcp.port 5555 ");
                    // p.getOutputStream().write("ls -ll".getBytes());
                    p.waitFor();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        p.waitFor(5, TimeUnit.SECONDS);
                    }
                    p.getOutputStream().flush();
                    Log.d(TAG, "stop adbd");
                    p = Runtime.getRuntime().exec("setprop ctl.stop adbd");
                    p.waitFor();
                    p.getOutputStream().flush();

                    Log.d(TAG, "start adbd");
                    p = Runtime.getRuntime().exec(" setprop ctl.start adbd");
                    p.waitFor();


                } catch (IOException e) {
                    // Handle IOException (e.g., log the error)
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // Handle InterruptedException
                    e.printStackTrace();
                }
            }

        });


        findViewById(R.id.disable_adb_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Process  p = Runtime.getRuntime().exec("setprop service.adb.tcp.port -1 ");
                    p.waitFor();

                    p = Runtime.getRuntime().exec("setprop ctl.stop adbd");
                    p.waitFor();


                    p = Runtime.getRuntime().exec(" setprop ctl.start adbd");
                    p.waitFor();

                } catch (IOException e) {
                    // Handle IOException (e.g., log the error)
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // Handle InterruptedException
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.copy_library_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AssetManager assetManager = getAssets();
                try {
                    InputStream inputStream = assetManager.open("scrcpy-server.jar");

                    //SPRING10
                    String pathName = "/data/sk/scrcpy-server.jar";
                    //APOLLO10 pro has different filesystem configuration
                    if(Build.MODEL.equals("QUALCOMM APOLLO10 PRO") || Build.MODEL.equals("APOLLO10 PRO"))
                        pathName = "/data/ss/scrcpy-server.jar";
                    //APOLLO2_12_PRO
                    if(Build.MODEL.equals("APOLLO2_12_PRO"))
                        pathName = "/data/ss/scrcpy-server.jar";

                    Log.d(TAG, "pathName: " + pathName);

                    File outputFile = new File(pathName);

                    // outputFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(outputFile);


                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    inputStream.close();
                    outputStream.close();


                    Process p = Runtime.getRuntime().exec("chmod 777 /data/sk/scrcpy-server.jar ");
                    p.waitFor();
                    //su", "chmod", "777", "/data/local/tmp/$targetFileName";


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //ALL in ONE, for easy test by the customer:
        findViewById(R.id.generic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.start_btn).performClick();
                findViewById(R.id.enable_adb_btn).performClick();
                findViewById(R.id.copy_library_btn).performClick();

                Toast.makeText(MainActivity.this, "DONE!", Toast.LENGTH_SHORT).show();
            }
        });
/*
        findViewById(R.id.start_btn).setVisibility(View.GONE);
        findViewById(R.id.stop_btn).setVisibility(View.GONE);
        findViewById(R.id.check_btn).setVisibility(View.GONE);
        findViewById(R.id.enable_adb_btn).setVisibility(View.GONE);
        findViewById(R.id.disable_adb_btn).setVisibility(View.GONE);
        findViewById(R.id.copy_library_btn).setVisibility(View.GONE);*/
    }




}