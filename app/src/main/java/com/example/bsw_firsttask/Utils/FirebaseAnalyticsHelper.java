package com.example.bsw_firsttask.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.Constants.Constants;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class FirebaseAnalyticsHelper {

    public static void logCustomEvents(Context context,String eventName, Bundle params){

        try {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, params);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void setUserProperty(Context context, String userProperty, String input){

        try{
            FirebaseAnalytics.getInstance(context).setUserProperty(userProperty,input);

        }catch (Exception e){

            e.printStackTrace();
        }

    }
}
