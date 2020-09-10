package com.example.bsw_firsttask.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class Screenshot extends AsyncTask<Void,Void,File>{

    public AsyncResponse delegate = null;
    private WeakReference<Activity> activityReference;
    private Bitmap bitmap;
    private File imageFile;

    Screenshot(Activity activity,AsyncResponse response){
        activityReference = new WeakReference<>(activity);
        this.delegate = response;
    }

    @Override
    protected void onPreExecute() {

        if(activityReference != null){

            View v = activityReference.get().getWindow().getDecorView().getRootView();
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache(true);
            bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

        }
    }

    @Override
    protected File doInBackground(Void... voids) {

        if(activityReference != null){

            long n  = System.currentTimeMillis() / 1000L;
            String fname = "image-" + n + ".jpg";
            imageFile = new File(activityReference.get().getApplicationContext().getExternalFilesDir(null), fname);

            try {

                FileOutputStream out = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                out.flush();
                out.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return imageFile;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {

        if(activityReference != null && activityReference.get()!= null && imageFile != null){
            delegate.processFinish(imageFile);
        }else{
            delegate.processFinish(null);
        }
    }

    public interface AsyncResponse {
        void processFinish(File output);
    }
}
