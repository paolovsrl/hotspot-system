package com.omsi.softap.api23;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class API23 {


    private static final String TAG = "SoftApManager23";

    static final int PSK_MIN_LEN = 8;
    static final int PSK_MAX_LEN = 63;


    static final int RESULT_OK = 1;
    static final int RESULT_FAILED = -1;
    static final int INVALID_CONFIG = 2;



    public static int startWithAPI23(Activity activity, String ssid, String password, boolean isOpen){
        if(!isOpen && ( password.length() < PSK_MIN_LEN || password.length() > PSK_MAX_LEN) || ssid==null || ssid.isEmpty()){
            return INVALID_CONFIG;
        }

        //Configuration:
        WifiConfiguration apConfig = new WifiConfiguration();
        WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apConfig.SSID = ssid;
        apConfig.allowedKeyManagement.set(/*WifiConfiguration.KeyMgmt.WPA2_PSK*/4); //System access only.
        apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        //apConfig.apChannel = 11; //hidden

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



}
