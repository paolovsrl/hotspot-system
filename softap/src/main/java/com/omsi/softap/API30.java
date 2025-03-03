package com.omsi.softap;

import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_OPEN;
import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_WPA2_PSK;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class API30 {
    private static final String TAG = "SoftApManager23";

    static final int PSK_MIN_LEN = 8;
    static final int PSK_MAX_LEN = 63;


    static final int RESULT_OK = 1;
    static final int RESULT_FAILED = -1;
    static final int INVALID_CONFIG = 2;


    public static int startWithAPI30(Activity activity, String ssid, String password, boolean isOpen){
        //Does not work, I believe methods have been hidden in latest OS. Check: https://dl.google.com/developers/android/rvc/non-sdk/hiddenapi-flags.csv
        if(!isOpen && ( password.length() < PSK_MIN_LEN || password.length() > PSK_MAX_LEN) || ssid==null || ssid.isEmpty()){
            return INVALID_CONFIG;
        }

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
                        //  t = m.invoke(t, "HOTSPOT_A");
                        t = m.invoke(t, ssid);
                        Log.d(TAG, "Set Hotspot name");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                        return INVALID_CONFIG;
                    }

                }
            }

            for (Method m : allMethods) {
                Log.d(TAG, "Method: " + m.getName());
                if (m.getName().equals("setPassphrase")) {
                    try {
                        m.setAccessible(true);
                        if(isOpen)
                            t = m.invoke(t, null, SECURITY_TYPE_OPEN); //0--> OPEN
                        else{
                            t = m.invoke(t, password, SECURITY_TYPE_WPA2_PSK); //0--> OPEN
                        }
                        Log.d(TAG, "Set Hotspot passphrase");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                        return INVALID_CONFIG;
                    }

                }
            }

            for (Method m : allMethods) {
                Log.d(TAG, "Method: " + m.getName());
                if (m.getName().equals("setAutoShutdownEnabled")) {
                    try {
                        m.setAccessible(true);

                        t = m.invoke(t, false); //0--> OPEN
                        Log.d(TAG, "Disable Autoshutdown");

                        // Handle any exceptions thrown by method to be invoked.
                    } catch (Exception x) {
                        x.printStackTrace();
                        return INVALID_CONFIG;
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
                        return INVALID_CONFIG;
                    }

                }
            }


            if (config != null){
                Log.d(TAG, "SoftAP configured!");
            }



        } catch (Exception e){
            return RESULT_FAILED;
        }

        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            //Difference with startLocalOnlyHotspot?
            Method setConfigMethod = mWifiManager.getClass().getMethod("startTetheredHotspot", SoftApConfiguration());
            boolean status = (boolean) setConfigMethod.invoke(mWifiManager, config);
            Log.d(TAG, "setWifiApConfiguration - success? " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error in configureHotspot");
            e.printStackTrace();
            return RESULT_FAILED;
        }

        return RESULT_OK;
    }



    private static Class SoftApConfiguration() {
        try {
            //  return Class.forName("android.net.wifi.SoftApConfiguration$Builder");
            return Class.forName("android.net.wifi.SoftApConfiguration");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }




    public static int stopWithAPI30(Activity activity) {
        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method stopSoftAp = mWifiManager.getClass().getMethod("stopSoftAp");
            boolean status = (boolean) stopSoftAp.invoke(mWifiManager);
            Log.d(TAG, "stop AP - success? " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error in stopping Hotspot");
            e.printStackTrace();
            return RESULT_FAILED;
        }

        return RESULT_OK;
    }



}
