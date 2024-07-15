package com.omsi.softap;

import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_OPEN;
import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_WPA2_PSK;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SoftApManager {

    private static final String TAG = "SoftApManager";

    static final int PSK_MIN_LEN = 8;
    static final int PSK_MAX_LEN = 63;


    static final int RESULT_OK = 1;
    static final int RESULT_FAILED = -1;
    static final int INVALID_CONFIG = 2;




    /**
     * Requires: android.permission.TETHER_PRIVILEGED, which is only granted to system apps.
     */
    public static int startSoftAp(Activity activity, String ssid, String password, boolean isOpen){



        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            //Android 11
            return startWithAPI30(activity, ssid, password, isOpen);
        }else if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P){
            //android9
            return startWithAPI28(activity, ssid, password, isOpen);
        } else{
            //android6
            return startWithAPI23(activity, ssid, password, isOpen);
        }



    }




    public static int stopSoftAp(Activity activity){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
            return stopWithAPI30(activity);
        } else if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P){
            //android9
            return stopWithAPI28(activity);
        }else{
            return stopWithAPI23(activity);
        }
    }






    public static boolean isSoftApActive(Activity activity) {
        ConnectivityManager mConnectivityManager;
        mConnectivityManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(ConnectivityManager.class);
        try {
            Method method = mConnectivityManager.getClass().getDeclaredMethod("getTetheredIfaces");
            if (method == null) {
                Log.e(TAG, "getTetheredIfaces is null");
            } else {
                String res[] = (String []) method.invoke(mConnectivityManager, null);
              /*  Log.d(TAG, "getTetheredIfaces invoked");
                Log.d(TAG, Arrays.toString(res));*/
                if (res.length > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getTetheredIfaces");
            e.printStackTrace();
        }
        return false;
    }







    /*************API23*************************************************************************************/

    public static int startWithAPI23(Activity activity, String ssid, String password, boolean isOpen){
        if(!isOpen && ( password.length() < PSK_MIN_LEN || password.length() > PSK_MAX_LEN) || ssid==null || ssid.isEmpty()){
            return INVALID_CONFIG;
        }

        //Configuration:
        WifiConfiguration apConfig = new WifiConfiguration();
        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apConfig.SSID = ssid;
        if (isOpen){
            //apConfig.setSecurityParams(WifiConfiguration.SECURITY_TYPE_OPEN);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else{
            apConfig.preSharedKey = password;
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK); //Default.
        }
        try {
            Method setWifiApEnabled = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean status = (boolean) setWifiApEnabled.invoke(mWifiManager, apConfig,true);
            Log.d(TAG, "Enable AP - success? " + status);
            if(!status)
                return RESULT_FAILED;
        } catch (Exception e) {
            Log.e(TAG, "Error in enabling Hotspot");
            e.printStackTrace();
            return INVALID_CONFIG;
        }


           /* Method tether = mConnectivityManager.getClass().getDeclaredMethod("tether", String.class);
            String iface = "wlan0";
            int result = (int) tether.invoke(mConnectivityManager, iface);
            //0=no error, 1=unknown iface, 2=service unavailable ... >2 not working
            Log.d(TAG, "tether invoked with result: "+result);
*/
            //Wifi must be enabled to have 0 as returned value

        return RESULT_OK;
    }




    public static  int stopWithAPI23(Activity activity) {

        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration apConfig = new WifiConfiguration();
        try {
            Method setWifiApEnabled = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean status = (boolean) setWifiApEnabled.invoke(mWifiManager, apConfig,false);
            Log.d(TAG, "Disable AP - success? " + status);
            if(!status)
                return RESULT_FAILED;

        } catch (Exception e) {
            Log.e(TAG, "stopTethering error: " + e.toString());
            e.printStackTrace();
            return RESULT_FAILED;
        }

        return RESULT_OK;
    }







    /*************API28*************************************************************************************/

    //Setting a password does not work!
    public static int startWithAPI28(Activity activity, String ssid, String password, boolean isOpen){
        if(!isOpen && ( password.length() < PSK_MIN_LEN || password.length() > PSK_MAX_LEN) || ssid==null || ssid.isEmpty()){
            return INVALID_CONFIG;
        }

        //Configuration:
        WifiConfiguration apConfig = new WifiConfiguration();
        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apConfig.SSID = ssid;
        if (isOpen){
           //apConfig.setSecurityParams(WifiConfiguration.SECURITY_TYPE_OPEN);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else{
            apConfig.preSharedKey = password;
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK); //Default.
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






    private static Class OnStartTetheringCallbackClass() {
        try {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }






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


/*******************************************************************************************************/
/*************API30*************************************************************************************/


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
