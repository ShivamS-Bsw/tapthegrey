package com.example.bsw_firsttask.Callbacks;

import com.google.android.gms.ads.formats.UnifiedNativeAd;

public interface NativeAdCallback {

    void nativeAd(UnifiedNativeAd unifiedNativeAd);
    void onAdOpen();
    void onAdClosed();
    void onAdLoaded();
    void onAdFailedToLoad();
}
