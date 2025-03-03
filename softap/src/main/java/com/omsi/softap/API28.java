package com.omsi.softap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class API28 {

    private static final String TAG = "SoftApManager28";

    static final int PSK_MIN_LEN = 8;
    static final int PSK_MAX_LEN = 63;


    static final int RESULT_OK = 1;
    static final int RESULT_FAILED = -1;
    static final int INVALID_CONFIG = 2;



    //Setting a password does not work!
    @SuppressLint("NewApi")
    public int startWithAPI28(Activity activity, String ssid, String password, boolean isOpen){
        if(!isOpen && ( password.length() < PSK_MIN_LEN || password.length() > PSK_MAX_LEN) || ssid==null || ssid.isEmpty()){
            return INVALID_CONFIG;
        }

        //Configuration:
        WifiConfiguration apConfig = new WifiConfiguration();
        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apConfig.SSID = ssid;
        if (isOpen){
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else{
            apConfig.preSharedKey = password;
            apConfig.allowedKeyManagement.set(/*WifiConfiguration.KeyMgmt.WPA2_PSK*/4); //System access only.
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }
        try {
            Method setConfigMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            boolean status = (boolean) setConfigMethod.invoke(mWifiManager, apConfig);
            Log.d(TAG, "setWifiApConfiguration - success? " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error in configureHotspot");
            e.printStackTrace();
            return INVALID_CONFIG;
        }




        MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback() {
            @Override
            public void onTetheringStarted() {
                Log.d(TAG, "Hotspot active");
            }

            @Override
            public void onTetheringFailed() {

            }
        };

        File outputDir = activity.getCodeCacheDir();
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
            return RESULT_FAILED;
        }


        ConnectivityManager mConnectivityManager;
        mConnectivityManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(ConnectivityManager.class);
        Method method = null;
        try {
            method = mConnectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, OnStartTetheringCallbackClass(), Handler.class);
            if (method == null) {
                Log.e(TAG, "startTetheringMethod is null");
                return RESULT_FAILED;
            } else {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE, false, proxy, null);
                Log.d(TAG, "startTethering invoked");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enableTethering");
            e.printStackTrace();
            return RESULT_FAILED;
        }

        return RESULT_OK;
    }





//Proxy
    private static Class OnStartTetheringCallbackClass() {
        try {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }


    public abstract class MyOnStartTetheringCallback {
        /**
         * Called when tethering has been successfully started.
         */
        public abstract void onTetheringStarted();

        /**
         * Called when starting tethering failed.
         */
        public abstract void onTetheringFailed();

    }


    @SuppressLint("NewApi")
    public static  int stopWithAPI28(Activity activity) {
        ConnectivityManager mConnectivityManager;
        mConnectivityManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(ConnectivityManager.class);
        try {
            Method method = mConnectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            if (method == null) {
                Log.e(TAG, "stopTetheringMethod is null");
                return RESULT_FAILED;
            } else {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE);
                Log.d(TAG, "stopTethering invoked");
            }
        } catch (Exception e) {
            Log.e(TAG, "stopTethering error: " + e.toString());
            e.printStackTrace();
            return RESULT_FAILED;
        }

        return RESULT_OK;
    }


}
