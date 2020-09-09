package com.example.bsw_firsttask.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.lang.ref.WeakReference;

public class DeviceInfo {

    private static DeviceInfo deviceInfo;
    private static WeakReference<Activity> activityWeakReference = null;

    private DeviceInfo(){

    }

    public static DeviceInfo getInstance(Activity activity){
        if(deviceInfo == null){

            deviceInfo = new DeviceInfo();
            activityWeakReference = new WeakReference<>(activity);
        }

        return  deviceInfo;
    }

    private Activity getActivityRef() {
        return activityWeakReference != null ? activityWeakReference.get() : null;
    }

    public String getDeviceInfo(){

        try{

            if(getActivityRef() != null ){


                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) getActivityRef().getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(memoryInfo);

                double availableMegs = memoryInfo.availMem / 0x100000L; // Available RAM Space
                double totalMem = memoryInfo.totalMem / 0x100000L; // Total RAM Space

                String deviceInfo = "OS Version : " + Build.VERSION.RELEASE
                        + "\nProcessor : " + Build.HARDWARE
                        + "\nDevice Name: " + Build.MANUFACTURER + " " + Build.MODEL
                        + "\nAvailable Ram Space: " + availableMegs + " MB"
                        + "\nTotal Space: " + totalMem + " MB";

                return deviceInfo;
            }

        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return null;
    }

    public String getNetworkType(){

        if(getActivityRef() != null ){

            ConnectivityManager cm = (ConnectivityManager) getActivityRef().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();

            if(info != null){

                if(info.isConnected()){

                    if(info.getType() == ConnectivityManager.TYPE_WIFI)
                        return "Wifi";
                    else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
                        int networkType = info.getSubtype();
                        switch (networkType) {
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN: {
                                return "2G";
                            }
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_HSPAP: {
                                return "3G";
                            }
                            case TelephonyManager.NETWORK_TYPE_LTE: {
                                return "4G";
                            }
                            default:
                        }
                    }
                }
            }
            return "Not Connected";
        }
        return null;
    }

}
