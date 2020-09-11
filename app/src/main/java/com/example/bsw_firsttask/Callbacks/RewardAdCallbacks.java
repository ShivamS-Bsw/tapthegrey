package com.example.bsw_firsttask.Callbacks;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public interface RewardAdCallbacks {

//    public void onRewardAddOpen();
    public void onRewardAddClose();
    public void onRewardAddEarnedItem(RewardItem rewardItem);
//    public void onRewardAddFailedtoLoad(AdError adError);

}
