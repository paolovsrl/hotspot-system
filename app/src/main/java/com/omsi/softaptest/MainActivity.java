package com.omsi.softaptest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_btn_deprecated).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithDeprecatedMethod();
            }
        });

        findViewById(R.id.start_btn_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithAPI30();
            }
        });
    }


    private void startWithDeprecatedMethod(){

        configureHotspot("AndroidAP", "123456789");

        MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback() {
            @Override
            public void onTetheringStarted() {
                Log.d(TAG, "Hotspot active");
            }

            @Override
            public void onTetheringFailed() {

            }
        };

        File outputDir = getCodeCacheDir();
        Object proxy;
        try {
            proxy = ProxyBuilder.forClass(OnStartTetheringCallbackClass())
                    .dexCache(outputDir).handler(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "onTetheringStarted":
                                    callback.onTetheringStarted();
                                    break;
                                case "onTetheringFailed":
                                    callback.onTetheringFailed();
                                    break;
                                default:
                                    ProxyBuilder.callSuper(proxy, method, args);
                            }
                            return null;
                        }

                    }).build();
        } catch (Exception e) {
            Log.e(TAG, "Error in enableTethering ProxyBuilder");
            e.printStackTrace();
            return;
        }


        ConnectivityManager mConnectivityManager;
        mConnectivityManager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        Method method = null;
        try {
            method = mConnectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, OnStartTetheringCallbackClass(), Handler.class);
            if (method == null) {
                Log.e(TAG, "startTetheringMethod is null");
            } else {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE, false, proxy, null);
                Log.d(TAG, "startTethering invoked");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enableTethering");
            e.printStackTrace();
        }
    }


    /**
     * Requires: android.permission.TETHER_PRIVILEGED, which is only granted to system apps.
     * However it does not work, throws an exception:  App not allowed to read or update stored WiFi AP config (uid = 10125)
     */
    public void configureHotspot(String name, String password) {
        WifiConfiguration apConfig = new WifiConfiguration();
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        apConfig.SSID = name;
        apConfig.preSharedKey = password;
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        try {
            Method setConfigMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            boolean status = (boolean) setConfigMethod.invoke(mWifiManager, apConfig);
            Log.d(TAG, "setWifiApConfiguration - success? " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error in configureHotspot");
            e.printStackTrace();
        }
    }


    private Class OnStartTetheringCallbackClass() {
        try {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }










    void startWithAPI30(){
        //Does not work, I believe methods have been hidden in latest OS. Check: https://dl.google.com/developers/android/rvc/non-sdk/hiddenapi-flags.csv

        //Creating the configuration:

        Object config = null;
        try {
            Class<?> c = Class.forName("android.net.wifi.SoftApConfiguration$Builder");
            Object t = c.newInstance();

            Method[] allMethods = c.getDeclaredMethods();

            for (Method m : allMethods) {
                Log.d(TAG, "Method: "+m.getName());
                if (m.getName().equals("setSsid")) {

                    try {
                        m.setAccessible(true);
                        t = m.invoke(t, "HOTSPOT_A");
                        //setPhasspras   o = m.invoke(t, null, 0); //0--> OPEN
                        Log.d(TAG, "Set Hotspot name");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                    }

                }
            }

            for (Method m : allMethods) {
                Log.d(TAG, "Method: " + m.getName());
                if (m.getName().equals("setPassphrase")) {
                    try {
                        m.setAccessible(true);
                        t = m.invoke(t, null, 0); //0--> OPEN
                        Log.d(TAG, "Set Hotspot passphrase");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                    }

                }
            }


            for (Method m : allMethods) {
                if (m.getName().equals("build")) {
                    try {
                        m.setAccessible(true);
                        config = m.invoke(t, (Object[]) null); //0--> OPEN
                        Log.d(TAG, "Build Hotspot config");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                    }

                }
            }


            if (config != null){
                Log.d(TAG, "OK");
            }



        } catch (Exception e){

        }

        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try {
            Method setConfigMethod = mWifiManager.getClass().getMethod("startTetheredHotspot", SoftApConfiguration());
            boolean status = (boolean) setConfigMethod.invoke(mWifiManager, config);
            Log.d(TAG, "setWifiApConfiguration - success? " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error in configureHotspot");
            e.printStackTrace();
        }
    }



    private Class SoftApConfiguration() {
        try {
          //  return Class.forName("android.net.wifi.SoftApConfiguration$Builder");
              return Class.forName("android.net.wifi.SoftApConfiguration");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

}