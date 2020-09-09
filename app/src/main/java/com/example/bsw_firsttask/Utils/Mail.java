package com.example.bsw_firsttask.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.bsw_firsttask.BuildConfig;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class Mail {


    private static Mail mail;
    private static WeakReference<Activity> activityWeakReference = null;

    private Mail(){

    }

    public static Mail getInstance(Activity activity){
        if(mail == null){

            mail = new Mail();
            activityWeakReference = new WeakReference<>(activity);
        }

        return  mail;
    }

    private Activity getActivityRef() {
        return activityWeakReference != null ? activityWeakReference.get() : null;
    }

    public void sendEmail(int scoreCount){

        if(getActivityRef() != null ){

            // Hardcoded as of now
            String email = "shivam@bswgames.com";
            String subject = "Issue in the game";
            String body = "Feedback: " + "\n"+
                    "Network Type: " + DeviceInfo.getInstance(getActivityRef()).getNetworkType() + "\n" +
                    "Score: " + scoreCount + "\n" +
                    "Device Info: " + "\n"+
                    DeviceInfo.getInstance(getActivityRef()).getDeviceInfo();

            try{

                File imageFile = Screenshot.getInstance().takeScreenshot(getActivityRef());

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT,body);

                emailIntent.setType("image/*"); // accept any image
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Uri uri = null;

                if (imageFile != null)
                    uri = FileProvider.getUriForFile(getActivityRef(), BuildConfig.APPLICATION_ID + ".provider",imageFile);

                if(uri != null)
                    emailIntent.putExtra(Intent.EXTRA_STREAM,uri);

                PackageManager pm = getActivityRef().getPackageManager();
                List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
                ResolveInfo best = null;

                for(ResolveInfo info : matches)
                    if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                        best = info;


                if (best != null)
                    emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);

                getActivityRef().startActivity(emailIntent);

            }catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

}
