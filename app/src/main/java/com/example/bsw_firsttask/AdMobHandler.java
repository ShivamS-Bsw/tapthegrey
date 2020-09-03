package com.example.bsw_firsttask;

import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.Callbacks.ExitInterstitialAdCallback;
import com.example.bsw_firsttask.Callbacks.HomeInterstitialAdCallback;
import com.example.bsw_firsttask.Callbacks.NativeAdCallback;
import com.example.bsw_firsttask.Callbacks.RewardAdCallbacks;
import com.example.bsw_firsttask.Callbacks.RewardedAdLoadCallbacks;
import com.example.bsw_firsttask.Factory.Constants;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.lang.ref.WeakReference;

// where you pause & resume your banner ads.
public class AdMobHandler {

    private static AdMobHandler adMobHandler;
    private InterstitialAd exitInterstitialAd = null;
    private static WeakReference<Activity> activityWeakReference = null;
    private ExitInterstitialAdCallback exitInterstitialAdCallback;
    private RewardedAd rewardedAd;
    private RewardedAdLoadCallbacks rewardedAdLoadCallback;
    private RewardAdCallbacks rewardedAdCallback;
    public static boolean iSshowRewardedLoaded = false ;
    private Handler nativeHandler;
    public UnifiedNativeAd unifiedNativeAd;
    private NativeAdCallback nativeAdCallback;

    private AdView adView;

    private AdMobHandler(){
    }

     synchronized public static AdMobHandler getInstance(Activity activity){

        if(adMobHandler == null){
            activityWeakReference = new WeakReference<>(activity);
            adMobHandler = new AdMobHandler();
        }
        return adMobHandler;
    }

    public void initClass(){

        try{

            Activity activity = getActivityRef();
            if(activity != null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAds();
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initAds() {

        if(getActivityRef() != null){

            initExitInterstitialAd();
            loadNativeAd();
            initRewardedAd();
        }
    }
    public void loadAdView(){

        Activity activity = getActivityRef();

        if (adView != null && activity != null) {
            ((LinearLayout) activity.findViewById(R.id.ad_layout)).removeView(adView);
            adView = null;
        }

        if(activity !=null){

            LinearLayout adParent = activity.findViewById(R.id.ad_layout);
            adParent.setVisibility(View.VISIBLE);
            adView = new AdView(activity);

            AdSize adSize = getAdSize();
            adView.setAdSize(adSize);

            adView.setAdUnitId(Constants.BANNER_ADunit_ID);
            adView.loadAd(getAdRequest());
            adParent.addView(adView);
        }
    }

    private AdLoader adLoader;


    public void loadNativeAd(){

        final Activity activity = getActivityRef();
        if(activity != null){

            nativeHandler = new Handler();

            AdLoader.Builder builder = new AdLoader.Builder(activity,Constants.NATIVE_ADunit_ID);
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {

                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAds) {


                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds);
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getCallToAction());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getExtras());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getAdvertiser());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getMediationAdapterClassName());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getStore());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getIcon());
                        Log.e("UnifiedNativeAd", " " + unifiedNativeAds.getStarRating());

               // Add a callback and pass the value to dialog fragment
                        setUnifiedNativeAd(unifiedNativeAds);
                }
            });

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .build();

            builder.withNativeAdOptions(adOptions);

            adLoader = builder.withAdListener(new AdListener(){

                @Override
                public void onAdOpened() {

                    Log.i("Native Ad","Ad Opened");
                    if(nativeAdCallback!=null)
                        nativeAdCallback.onAdOpen();
                }
                @Override
                public void onAdLoaded() {

                    Log.i("Native Ad","Ad Loaded");
                    if(nativeAdCallback!=null)
                        nativeAdCallback.onAdLoaded();

                    if (nativeHandler != null)
                            nativeHandler.postDelayed(nativeRunnable, 1000 * 60);
                }
                @Override
                public void onAdFailedToLoad(int errorCode) {

                    Log.i("Native Ad","Ad Failed to Load");
                    if(nativeAdCallback!=null)
                        nativeAdCallback.onAdFailedToLoad();

                    if (nativeHandler != null)
                            nativeHandler.postDelayed(nativeRunnable, 1000 * 60);
                }
                @Override
                public void onAdClicked() {

                    Log.d("UnifiedNativeAd", "onAdClicked: ");
                    if (nativeHandler != null)
                            nativeHandler.removeCallbacks(nativeRunnable);
                }

                @Override
                public void onAdClosed() {

                    Log.i("Native Ad","Ad Closed");
                    if(nativeAdCallback!=null)
                        nativeAdCallback.onAdClosed();
                }
            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    /**
     * you only remove callwhen  i only click on ad what if i leave the screen.
     */
    Runnable nativeRunnable = new Runnable() {
        @Override
        public void run() {
            if (adLoader != null)
                 adLoader.loadAd(new AdRequest.Builder().build());
        }
    };

    /**
     * when you again try to send request if you alredy show reward ads?
     */

    public void initRewardedAd(){

        Activity activity = getActivityRef();

        if(activity != null){

            rewardedAd = new RewardedAd(activity,Constants.REWARDED_ADunit_ID);
            RewardedAdLoadCallback callback = new RewardedAdLoadCallback(){

                @Override
                public void onRewardedAdLoaded() {
                    if(rewardedAdLoadCallback != null){

                        Log.d("Rewarded Ad","Loading Complete");
                        rewardedAdLoadCallback.onLoadCompleted();
                    }
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                    if(rewardedAdLoadCallback != null){

                        Log.d("Rewarded Ad","Loading Failed");
                        rewardedAdLoadCallback.onLoadFailed();
                    }
                }
            };
                rewardedAd.loadAd(getAdRequest(),callback);
        }

    }
    public void initExitInterstitialAd(){

        Activity activity = getActivityRef();

        if(activity != null) {

            exitInterstitialAd = new InterstitialAd(activity);
            exitInterstitialAd.setAdUnitId(Constants.INTERSTITIAL_ADunit_ID);
            exitInterstitialAd.setAdListener(new AdListener(){

                @Override
                public void onAdOpened() {

                    if(exitInterstitialAdCallback!=null)
                        exitInterstitialAdCallback.onAdOpen();
                }

                @Override
                public void onAdClosed() {

                    if(exitInterstitialAd != null){
                        exitInterstitialAdCallback.onAdClosed();
                    }

                    requestExitInterstitial();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    if(exitInterstitialAd != null) {
                        exitInterstitialAdCallback.onLoadingCompleted();
                    }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);

                    if(exitInterstitialAd != null) {
                        exitInterstitialAdCallback.onLoadingFailed();
                    }
                }
            });

            requestExitInterstitial();
        }
    }


    private Activity getActivityRef() {
        return activityWeakReference != null ? activityWeakReference.get() : null;
    }

    private void requestExitInterstitial() {
        if (exitInterstitialAd != null && !exitInterstitialAd.isLoaded() && !exitInterstitialAd.isLoading()) {
            exitInterstitialAd.loadAd(getAdRequest());
        }
    }

    private AdRequest getAdRequest(){
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    public boolean isRewardedLoaded(){

        return rewardedAd != null && rewardedAd.isLoaded();
    }



    public boolean isExitInterstitialLoaded(){

        if (exitInterstitialAd != null && !exitInterstitialAd.isLoaded() && !exitInterstitialAd.isLoading()) {
            return false;
        }
        return true;
    }
    // What if reward ad "rewardedAd.isLoaded()" is not loaded?
    public void showRewardedAd(){

        Activity activityContext = getActivityRef();

        if(activityContext != null){

            if(rewardedAd != null && rewardedAd.isLoaded() ){

                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddOpen();
                    }

                    @Override
                    public void onRewardedAdClosed() {

                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddClose();
                    }
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddEarnedItem(reward);
                    }
                    @Override
                    public void onRewardedAdFailedToShow(AdError adError) {
                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddFailedtoLoad(adError);
                    }
                };
                rewardedAd.show(activityContext,adCallback);
            }
        }
    }

    public void showExitIntAd(){

        Activity activity = getActivityRef();

        if(activity != null) {

            if (exitInterstitialAd != null && exitInterstitialAd.isLoaded()) {

                exitInterstitialAd.show();

            } else {
                if (exitInterstitialAd != null && !exitInterstitialAd.isLoaded() && !exitInterstitialAd.isLoading()) {
                    requestExitInterstitial();
                }
            }
        }
    }


    public void setExitInterstitialAdCallback(ExitInterstitialAdCallback exitInterstitialAdCallback) {
        this.exitInterstitialAdCallback = exitInterstitialAdCallback;
    }

    private AdSize getAdSize() {

        // Determine the screen width (less decorations) to use for the ad width.
        Activity activityContext = getActivityRef();
        Display display = activityContext.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = 0/*adContainerView.getWidth()*/;

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationBannerAdSizeWithWidth(activityContext, adWidth);
    }

    public void setRewardedAdCallback(RewardAdCallbacks rewardedAdCallback) {
        this.rewardedAdCallback = rewardedAdCallback;
    }

    public void setRewardedAdLoadCallback(RewardedAdLoadCallbacks rewardedAdLoadCallback) {
        this.rewardedAdLoadCallback = rewardedAdLoadCallback;
    }

    public void setNativeAdCallback(NativeAdCallback nativeAdCallback) {
        this.nativeAdCallback = nativeAdCallback;
    }

    public UnifiedNativeAd getUnifiedNativeAd() {
        return unifiedNativeAd;
    }

    public void setUnifiedNativeAd(UnifiedNativeAd unifiedNativeAd) {
        this.unifiedNativeAd = unifiedNativeAd;
    }
}
