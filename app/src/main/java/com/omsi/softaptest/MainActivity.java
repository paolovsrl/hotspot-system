package com.omsi.softaptest;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.omsi.softap.SoftApManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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


        findViewById(R.id.enable_adb_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              try {
                    Process  p = Runtime.getRuntime().exec("setprop service.adb.tcp.port 5555 ");
                  //  p.getOutputStream().write("ls -ll".getBytes());
                    p.waitFor();

                    p = Runtime.getRuntime().exec("stop adbd ");
                    p.waitFor();


                    p = Runtime.getRuntime().exec("start adbd ");
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

                    p = Runtime.getRuntime().exec("stop adbd ");
                    p.waitFor();


                    p = Runtime.getRuntime().exec("start adbd ");
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


                    Process p1 = Runtime.getRuntime().exec("touch /data/sk/tmp ");
                    p1.waitFor();

                    File outputFile = new File("/data/sk/scrcpy-server.jar");
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
    }


    private void executeShellCmd(String cmd){
        Process process = null;
        BufferedReader bufferedReader = null;
        StringBuilder output = new StringBuilder();

        File workingDirectory = new File("/data/local/tmp");
        String[] environment = {"PATH=/system/bin"};

        try {
            process = Runtime.getRuntime().exec(cmd,  environment, workingDirectory);
            bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = "";
            while((line = bufferedReader.readLine() )!= null){
                output.append(line);
            }
            process.waitFor();
            Log.d(TAG, "Result: "+output.toString());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}