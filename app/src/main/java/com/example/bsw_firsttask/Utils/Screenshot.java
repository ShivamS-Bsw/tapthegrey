package com.example.bsw_firsttask.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.bsw_firsttask.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class Screenshot extends AsyncTask<Void,Void,File>{

    public AsyncResponse delegate = null;
    private WeakReference<Activity> activityReference;
    private Bitmap bitmap;
    private File imageFile;
    private View view;

    Screenshot(Activity activity,AsyncResponse response,View view){
        activityReference = new WeakReference<>(activity);
        this.delegate = response;
        this.view = view;
    }

    @Override
    protected void onPreExecute() {

        if(view != null ){

            bitmap = Bitmap.createBitmap(view.getWidth(),
                    view.getHeight(), Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(bitmap));

        }
    }

    @Override
    protected File doInBackground(Void... views) {

        if(activityReference != null && bitmap != null){

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

            Log.d("Screenshot",imageFile.getAbsolutePath());
            return imageFile;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {

        if (activityReference != null && activityReference.get() != null && imageFile != null) {
            delegate.processFinish(imageFile);
        } else {
            delegate.processFinish(null);
        }
    }

    public interface AsyncResponse {
        void processFinish(File output);
    }
}
