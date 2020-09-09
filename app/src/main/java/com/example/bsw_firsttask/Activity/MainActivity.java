package com.example.bsw_firsttask.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.bsw_firsttask.AdMob.AdMobHandler;
import com.example.bsw_firsttask.BuildConfig;
import com.example.bsw_firsttask.Callbacks.ExitInterstitialAdCallback;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Fragments.GameOverScreen;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.Fragments.HomeScreen;
import com.example.bsw_firsttask.Fragments.SplashScreen;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ExitInterstitialAdCallback{

    public static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferencesManager preferencesManager;
    private ExitInterstitialAdCallback exitInterstitialAdCallback;
    public static boolean isAdMobInit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initClasses();
        initializeAdmMod();

        AdMobHandler.getInstance(MainActivity.this).initAllAds();

        if (savedInstanceState == null) {

            FactoryClass.getInstance().moveToNextScreen(this, Constants.SPLASHSCREEN_TAG, null, false);
        }

        if (savedInstanceState != null) {

            showLogs("Saved Instance State not null" + savedInstanceState);
        }

        if (BuildConfig.FLAVOR.equals("paid"))
            loadLocale();
    }

    private void initClasses() {
        preferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        showLogs("Saved Instance called");

        if (getSupportFragmentManager() != null)
            getSupportFragmentManager().putFragment(outState, "savedInstance", getSupportFragmentManager().findFragmentById(R.id.frame_container));
    }

    private void initializeAdmMod() {

        MobileAds.initialize(this);

        try {
            if (MobileAds.getInitializationStatus() == null)
                MobileAds.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
            MobileAds.initialize(this);
        }

    }

    @Override
    protected void onResume() {

        super.onResume();

        MediaHandler.getInstance(this);

        View decorView = getWindow().getDecorView();
        hideNavigationAndStatusBar(decorView);
        hideNavigationAndStatusBarListener(decorView);

    }

    @Override
    protected void onPause() {
        super.onPause();

        AdMobHandler.getInstance(this).onPause();
    }

    //   Method to Hide and Show the Navigation and Status Bar Listener
    private void hideNavigationAndStatusBarListener(final View decorView) {

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideNavigationAndStatusBar(decorView);
                }

            }
        });
    }

    private void hideNavigationAndStatusBar(View decorView) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);

        if (fragment instanceof HomeScreen || fragment instanceof SplashScreen) {
            ((HomeScreen) fragment).onBackPressed();

        } else if (fragment instanceof GameScreen) {

            ((GameScreen) fragment).showDialog();

        } else if (fragment instanceof GameOverScreen) {

            ((GameOverScreen)fragment).returnToHomeMenu();

        }
    }

    private void loadLocale() {

        setLocale(preferencesManager.getLocale());
    }

    public void setLocale(String s) {

        Locale locale = new Locale(s);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;

        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());

        //Save this data in shared pref
        preferencesManager.saveLocale(s);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MediaHandler.getInstance(this).destroySoundPool();
        AdMobHandler.getInstance(this).onClose();
    }

    public void finishActivity() {
        MainActivity.this.finish();
    }

    @Override
    public void onAdOpen() {
        HomeScreen.hideLoader();
    }

    @Override
    public void onAdClosed() {

        showLogs("Exit Int Ad Closed");
        finishActivity();
    }

    @Override
    public void onLoadingCompleted() {
        showLogs("Exit Int Ad Loaded");
    }

    @Override
    public void onLoadingFailed() {
        showLogs("Exit Int Ad Loading failed");
    }

    private void showLogs(String log) {
        Log.d(TAG, log);
    }

}
