package com.example.bsw_firsttask.AdMob;

import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.bsw_firsttask.Callbacks.ExitInterstitialAdCallback;
import com.example.bsw_firsttask.Callbacks.RewardAdCallbacks;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
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
    private RewardAdCallbacks rewardedAdCallback;
    private Handler nativeHandler;
    public UnifiedNativeAd unifiedNativeAd;

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

    public void initAllAds() {

        if(getActivityRef() != null){

            loadAdView();
            initExitInterstitialAd();
            loadNativeAd();
            initRewardedAd();
        }
    }
    private void loadAdView(){

        Activity activity = getActivityRef();

        if (adView != null && activity != null) {
            ((LinearLayout) activity.findViewById(R.id.ad_layout)).removeView(adView);
             adView = null;
        }

        if(activity != null){

            adView = new AdView(activity);

            AdSize adSize = getAdSize();
            adView.setAdSize(adSize);

            adView.setAdUnitId(Constants.BANNER_ADunit_ID);
            adView.loadAd(getAdRequest());

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    showLogs("Banner Ad Closed");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    showLogs("Banner Ad Failed " + errorCode);

                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    showLogs("Banner ad Opened");
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    showLogs("Banner ad Loaded");

                }
            });
        }
    }

    public void showBannerAd() {

        if(getActivityRef() != null && adView != null){

            if(adView.getParent() == null){

                LinearLayout adParent = (getActivityRef().findViewById(R.id.ad_layout));
                adParent.addView(adView);
               // adParent.setVisibility(View.VISIBLE);
            }
            if (adView != null) {
                adView.resume();
            }
        }else if(adView != null && !adView.isLoading() ){
            loadAdView();
        }
    }

//    private void hideBannerAd(){
//
//        if (getActivityRef() != null) {
//            LinearLayout adLayout = getActivityRef().findViewById(R.id.ad_layout);
//            adLayout.removeView(adView);
//
//            if (adLayout != null) {
//                adLayout.setVisibility(View.GONE);
//            }
//            if (adView != null) {
//                adView.pause();
//            }
//        }
//    }

    private AdLoader adLoader;

    public void loadNativeAd(){

        Activity activity = getActivityRef();
        if(activity != null){

            AdLoader.Builder builder = new AdLoader.Builder(activity,Constants.NATIVE_ADunit_ID);
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {

                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAds) {
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
                }
                @Override
                public void onAdLoaded() {
                    Log.i("Native Ad","Ad Loaded");
                }
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.i("Native Ad","Ad Failed to Load");
                }
                @Override
                public void onAdClicked() {
                    Log.d("UnifiedNativeAd", "onAdClicked: ");
                }
                @Override
                public void onAdClosed() {
                    Log.i("Native Ad","Ad Closed");
                }
            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    public boolean showNativeAd(){

        if(getActivityRef() != null){

            if(getUnifiedNativeAd() != null )
                return true;
            else if(adLoader != null && !adLoader.isLoading() ){
                loadNativeAd();
                return false;
            }
        }
        return false;
    }

    public void initRewardedAd(){

        Activity activity = getActivityRef();

        if(activity != null){

            rewardedAd = new RewardedAd(activity,Constants.REWARDED_ADunit_ID);
            RewardedAdLoadCallback callback = new RewardedAdLoadCallback(){

                @Override
                public void onRewardedAdLoaded() {
                        Log.d("Rewarded Ad","Loading Complete");
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d("Rewarded Ad","Loading Failed");
                }
            };
                rewardedAd.loadAd(getAdRequest(),callback);
        }

    }
    private void initExitInterstitialAd(){

        Activity activity = getActivityRef();

        if(activity != null) {

            exitInterstitialAd = new InterstitialAd(activity);
            exitInterstitialAd.setAdUnitId(Constants.INTERSTITIAL_ADunit_ID);
            exitInterstitialAd.setAdListener(new AdListener(){

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    showLogs("Interstitial Ad Opened");
                }

                @Override
                public void onAdClosed() {

                    if(exitInterstitialAdCallback != null)
                        exitInterstitialAdCallback.onAdClosed();

                    showLogs("Interstitial Ad Closed");
                    requestExitInterstitial();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    showLogs("Interstitial Ad Loaded");
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    showLogs("Interstitial Ad Failed to Load "+ loadAdError);
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

    public boolean showRewardedAd(){

        Activity activityContext = getActivityRef();

        if(activityContext != null){

            if(rewardedAd != null && rewardedAd.isLoaded() ){

                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdClosed() {

                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddClose();
                        initRewardedAd();
                    }
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        if(rewardedAdCallback != null)
                            rewardedAdCallback.onRewardAddEarnedItem(reward);
                    }
                };

                rewardedAd.show(activityContext,adCallback);
                return true;

            }else{
                initRewardedAd();

                return false;
            }
        }
        return false;
    }

    public boolean showExitIntAd(){

        Activity activity = getActivityRef();

        if(activity != null) {

            if (exitInterstitialAd != null && exitInterstitialAd.isLoaded()) {
                exitInterstitialAd.show();
                return true;

            } else if (exitInterstitialAd != null && !exitInterstitialAd.isLoaded() && !exitInterstitialAd.isLoading()) {
                    requestExitInterstitial();

                    return false;
            }
        }return false;
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

    public UnifiedNativeAd getUnifiedNativeAd() {
        return unifiedNativeAd;
    }

    public void setUnifiedNativeAd(UnifiedNativeAd unifiedNativeAd) {
        this.unifiedNativeAd = unifiedNativeAd;
    }
    private void showLogs(String msg){
        Log.d("AdHandler",msg);
    }


    public void onResume(){

        if(adView != null)
            adView.resume();
    }

    public void onPause() {

        if(adView != null)
            adView.pause();

    }
    public void onClose(){

        onDestroy();
        activityWeakReference = null;
        adMobHandler = null;

    }

    public void onDestroy(){

        if(adView != null){
            adView.destroy();
            adView = null;
        }

        if(unifiedNativeAd != null)
            unifiedNativeAd.destroy();

        exitInterstitialAd = null;
        rewardedAd = null;
    }
}
