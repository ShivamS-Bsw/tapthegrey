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

import com.example.bsw_firsttask.AdMobHandler;
import com.example.bsw_firsttask.BuildConfig;
import com.example.bsw_firsttask.Callbacks.ExitInterstitialAdCallback;
import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.FactoryClass;
import com.example.bsw_firsttask.Fragments.GameOverScreen;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.Fragments.HomeScreen;
import com.example.bsw_firsttask.Fragments.SplashScreen;
import com.example.bsw_firsttask.NetworkReceiver;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ExitInterstitialAdCallback,NetworkReceiver.ConnectivityReceiverListener {

    private Fragment fragmentClass;
    private FragmentManager fragmentManager;
    public static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferencesManager preferencesManager;
    private AdMobHandler adMobHandler;
    private ExitInterstitialAdCallback exitInterstitialAdCallback;
    public static boolean isAdMobInit = false;
    private NetworkReceiver.ConnectivityReceiverListener connectivityReceiverListener;
    private NetworkReceiver networkReceiver;
    public static boolean isNetworkConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting the destroyed instance back
        if(savedInstanceState != null){
            showLogs("Saved Instance State not null" + savedInstanceState);
            fragmentClass = getSupportFragmentManager().getFragment(savedInstanceState,"saved_fragment");
        }else{
            fragmentClass = new SplashScreen();
        }

        initClasses();
      //  initializeAdmMod();
        loadFragment(fragmentClass);

        if(BuildConfig.FLAVOR.equals("paid"))
            loadLocale();
    }

    private void initClasses() {
        preferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
//      adMobHandler = AdMobHandler.getInstance(this);

  //      exitInterstitialAdCallback = this;
//
//        adMobHandler.setExitInterstitialAdCallback(exitInterstitialAdCallback);
//
//        connectivityReceiverListener = this;
//        networkReceiver = new NetworkReceiver();
//        networkReceiver.setConnectivityReceiverListener(connectivityReceiverListener);
//
//        adMobHandler.initClass();
//
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        showLogs("Saved Instance called");
        //Function get called whenever OS kills the app in order to reclaim the memory
        if(getSupportFragmentManager() != null)
            getSupportFragmentManager().putFragment(outState,"saved_fragment",getSupportFragmentManager().findFragmentById(R.id.frame_container));
    }

    private void initializeAdmMod() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                isAdMobInit = true;
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        View decorView = getWindow().getDecorView();
        hideNavigationAndStatusBar(decorView);
        hideNavigationAndStatusBarListener(decorView);

       // this.registerReceiver(networkReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregisterReceiver(networkReceiver);
    }

  //   Method to Hide and Show the Navigation and Status Bar Listener
    private void hideNavigationAndStatusBarListener(final View decorView){

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // TODO: The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                    hideNavigationAndStatusBar(decorView);
                }

            }
        });
    }

    private void hideNavigationAndStatusBar(View decorView){


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

    private void loadFragment(Fragment fragment) {

        // load fragment
        fragmentManager  = getSupportFragmentManager();

        //For Debugging
//        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//
//                Fragment fragment1 = fragmentManager.findFragmentById(R.id.frame_container);
//                Log.i(TAG,fragment1.getClass().getSimpleName() + "  " + fragmentManager.getBackStackEntryCount());
//            }
//        });

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);

        if (fragment instanceof HomeScreen || fragment instanceof SplashScreen){

//            if(adMobHandler.isExitInterstitialLoaded()) {
//
//                HomeScreen.showLoader();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        // Show the add after 500ms of loading
//                        adMobHandler.showExitIntAd();
//                    }
//                },500);
//
//            }
//            else {
//                finishActivity();
//            }

            finishActivity();
        }
        else if(fragment instanceof GameScreen){

            ((GameScreen) fragment).showDialog();
        }
        else if(fragment instanceof GameOverScreen){

            // Intent to Home Screen frm this screen
               FactoryClass.moveToNextScreen(this,null, Constants.HOMESCREEN_TAG);

        }
    }

    private void loadLocale(){

        setLocale(preferencesManager.getLocale());
    }

    public void setLocale(String s) {

        Locale locale = new Locale(s);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;

        this.getResources().updateConfiguration(configuration,this.getResources().getDisplayMetrics());

        //Save this data in shared pref
        preferencesManager.saveLocale(s);
    }


    private void finishActivity(){
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

    private void showLogs(String log){
        Log.d(TAG,log);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected && adMobHandler != null) {

            isNetworkConnected = true;
//
            if(!adMobHandler.isRewardedLoaded())
                adMobHandler.initRewardedAd();
//
            if(!adMobHandler.isExitInterstitialLoaded())
                adMobHandler.initExitInterstitialAd();

        }else{
            isNetworkConnected = false;
        }
    }
}
