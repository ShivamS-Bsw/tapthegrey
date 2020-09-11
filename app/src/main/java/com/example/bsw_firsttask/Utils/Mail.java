package com.example.bsw_firsttask.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.bsw_firsttask.BuildConfig;
import com.example.bsw_firsttask.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Mail {

    private File imageFile = null ;
    private static Mail mail;
    private Uri uri = null;
    private Intent emailIntent;
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

                emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT,body);
                emailIntent.setType("image/*"); // accept any image
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                new Screenshot(getActivityRef(), new Screenshot.AsyncResponse() {
                    @Override
                    public void processFinish(File output) {
                        imageFile = output;
                    }
                },getActivityRef().findViewById(R.id.game_screen_parent)).execute();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (imageFile != null){

                            uri = FileProvider.getUriForFile(getActivityRef(), BuildConfig.APPLICATION_ID + ".provider",imageFile);
                            if(uri != null)
                                emailIntent.putExtra(Intent.EXTRA_STREAM,uri);

                        }else
                            Toast.makeText(getActivityRef().getApplicationContext(),"Error Taking Screenshot",Toast.LENGTH_SHORT).show();

                        ResolveInfo resolveInfo = getPakageInfo(emailIntent);
                        if (resolveInfo != null)
                            emailIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);

                        getActivityRef().startActivity(emailIntent);

                    }
                },1000);

            }catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    private ResolveInfo getPakageInfo(Intent intent){
        ResolveInfo best = null;

        if(getActivityRef() != null){
            PackageManager pm = getActivityRef().getPackageManager();
            List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);


            for(ResolveInfo info : matches)
                if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                    best = info;
        }

        return best;
    }
}
