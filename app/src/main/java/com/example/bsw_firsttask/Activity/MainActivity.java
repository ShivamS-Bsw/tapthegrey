package com.example.bsw_firsttask.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferencesManager preferencesManager;
    private ExitInterstitialAdCallback exitInterstitialAdCallback;
    public static boolean isAdMobInit = false;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private long lastInterstitialShown = 0;
    private ConstraintLayout activity;
    //private String imageUrl = "https://i.picsum.photos/id/1018/3914/2935.jpg?hmac=3N43cQcvTE8NItexePvXvYBrAoGbRssNMpuvuWlwMKg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initClasses();
        initializeAdmMod();


        if(firebaseRemoteConfig != null)
            fetchRemoteConfig();

        if(preferencesManager.getBG() == 1)
            loadBackground();
        else
            activity.setBackgroundResource(R.color.white);

       // getFirebaseRegistrationToken();

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

    private void initViews() {
        activity = findViewById(R.id.main_parent);
    }

    private void loadBackground() {

        Glide.with(this).load(R.drawable.bg_image)
                .placeholder(R.color.blue_gray)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        activity.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void initClasses() {


        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        preferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());

    }

    private void fetchRemoteConfig(){

        if(firebaseRemoteConfig != null){

            try{

                firebaseRemoteConfig.fetchAndActivate()
                        .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {

                                if( task == null || !task.isSuccessful()){
                                    showLogs("Unable to fetch remote config");

                                    return;
                                }

                                showLogs("Remote Config Fetch Complete");

                                String gameStartTimeInMillis = firebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_GAME_START_TIME);
                                String gameTimeInMillis = firebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_GAME_TIME);
                                String bg = firebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_BACKGROUND);

                                if(preferencesManager != null && gameStartTimeInMillis != null){
                                    preferencesManager.setGameStartTime(Integer.parseInt(gameStartTimeInMillis));

                                    showLogs("Game Start Time " + gameStartTimeInMillis);
                                }

                                if(preferencesManager != null && gameTimeInMillis != null){
                                    preferencesManager.setGameTime(Integer.parseInt(gameTimeInMillis));

                                    showLogs("Game Time " + gameTimeInMillis);
                                }

                                int intBG = Integer.parseInt(bg);
                                if(preferencesManager != null && preferencesManager.getBG() != intBG){
                                    preferencesManager.setBG(Integer.parseInt(bg));
                                    //recreate();
                                    showLogs("BG" + bg);

                                }
                            }
                        });
            }catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        showLogs("Saved Instance called");

        if (getSupportFragmentManager() != null)
            getSupportFragmentManager().putFragment(outState, "savedInstance", getSupportFragmentManager().findFragmentById(R.id.frame_container));
    }

    private void getFirebaseRegistrationToken(){

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d("Messaging", token);
                    }
                });
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

        AdMobHandler.getInstance(this).onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        showLogs("onPause");
        AdMobHandler.getInstance(this).onPause();

        overridePendingTransition(android.R.anim.slide_in_left,0);
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

        showLogs("onStop");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MediaHandler.getInstance(this).destroySoundPool();
        AdMobHandler.getInstance(this).onClose();
    }

    public boolean showExitInterstitialAd(){

        if(AdMobHandler.getInstance(this).showExitIntAd()){
            setLastInterstitialShown(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public boolean showRewardedVideoAd(){

        if(AdMobHandler.getInstance(this).showRewardedAd())
            return true;
        return false;
    }

    public void finishActivity() {
        MainActivity.this.finish();
    }

    private void showLogs(String log) {
        Log.d(TAG, log);
    }

    public long getLastInterstitialShown() {
        return lastInterstitialShown;
    }

    public void setLastInterstitialShown(long lastInterstitialShown) {
        this.lastInterstitialShown = lastInterstitialShown;
    }

}
