package com.omsi.softap;

import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_OPEN;
import static android.net.wifi.SoftApConfiguration.SECURITY_TYPE_WPA2_PSK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;
import com.omsi.softap.api23.API23;
import com.omsi.softap.api28.API28;
import com.omsi.softap.api30.API30;

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

    static final API23 api23 = new API23();
    static final API28 api28 = new API28();
    static final API30 api30 = new API30();


    /**
     * Requires: android.permission.TETHER_PRIVILEGED, which is only granted to system apps.
     */
    public static int startSoftAp(Activity activity, String ssid, String password, boolean isOpen){



        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            //Android 11
            return api30.startWithAPI30(activity, ssid, password, isOpen);
        }else if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P){
            //android9
            return api28.startWithAPI28(activity, ssid, password, isOpen);
        } else{
            //android6
            return api23.startWithAPI23(activity, ssid, password, isOpen);
        }



    }




    public static int stopSoftAp(Activity activity){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
            return api30.stopWithAPI30(activity);
        } else if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P){
            //android9
            return api28.stopWithAPI28(activity);
        }else{
            return api23.stopWithAPI23(activity);
        }
    }






    @SuppressLint("NewApi")
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




}
