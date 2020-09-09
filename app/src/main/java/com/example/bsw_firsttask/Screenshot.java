package com.example.bsw_firsttask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Screenshot  {


    private static Screenshot screenshot;
    private FileOutputStream fileOutputStream;

    private Screenshot(){
    }

    public static Screenshot getInstance(){
        return screenshot == null ? new Screenshot() : screenshot;
    }

    public File takeScreenshot(Activity activity) {

        File imageFile;

        if(activity != null){

            View v = activity.getWindow().getDecorView().getRootView();
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache(true);
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

            if(bitmap != null){
                imageFile = saveImageToExternalStorage(activity.getApplicationContext(),bitmap);
                return imageFile;
            }
    }
        return null;
    }

    private   File saveImageToExternalStorage(Context context, Bitmap finalBitmap) {


        long n  = System.currentTimeMillis() / 1000L;
        String fname = "image-" + n + ".jpg";

        File file = new File(context.getExternalFilesDir(null), fname);

        Log.d("Screenshot",file.getAbsolutePath());

        FirebaseCrashlytics.getInstance().setCustomKey("File_location",file.getAbsolutePath());

        try {

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();
        }
        catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return file;
    }
}
