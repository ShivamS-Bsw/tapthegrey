package com.example.bsw_firsttask.Fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.AdMobHandler;
import com.example.bsw_firsttask.Callbacks.RewardAdCallbacks;
import com.example.bsw_firsttask.Callbacks.RewardedAdLoadCallbacks;
import com.example.bsw_firsttask.CustomDialog;
import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.FactoryClass;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class GameOverScreen extends Fragment implements View.OnClickListener,RewardAdCallbacks,RewardedAdLoadCallbacks {

    private MediaPlayer mMediaPlayer;
    public static final String TAG = GameOverScreen.class.getSimpleName();
    private TextView points,best;
    private Button replay,home;
    private SharedPreferencesManager sharedPreferencesManager;
    private AdMobHandler adMobHandler;
    private RewardAdCallbacks rewardedAdCallback;
    private RewardedAdLoadCallbacks loadCallbacks;
    private boolean isAdClosed ;
    private boolean isReplayPressed;
    private RewardItem rewardItem;
    private String currentScore;

    private FrameLayout progressBar;
    public GameOverScreen(){
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_over,container,false);

        sharedPreferencesManager = SharedPreferencesManager.getInstance(getContext());

        //Clear the saved game
        if(sharedPreferencesManager.checkSavedGame())
            sharedPreferencesManager.clearSavedGame();

        sharedPreferencesManager.clearReplayGame();

        initViews(view);

        currentScore = getArguments().getString(Constants.CURRENT_SCORE);
        points.setText(String.valueOf(currentScore));


        if(Integer.parseInt(currentScore) >= sharedPreferencesManager.getSomeStringValue()){

            sharedPreferencesManager.setSomeStringValue(Integer.parseInt(currentScore));
            best.setText(String.valueOf(sharedPreferencesManager.getSomeStringValue()));

        }

        adMobHandler = AdMobHandler.getInstance(getActivity());
        adMobHandler.setRewardedAdCallback(rewardedAdCallback);
        adMobHandler.setRewardedAdLoadCallback(loadCallbacks);


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


        try {

            rewardedAdCallback = this;
            loadCallbacks = this;

        }catch (ClassCastException e){
            System.out.print(e.getMessage());
        }

    }

    @Override
    public void onDetach() {

        rewardedAdCallback = null;
        loadCallbacks = null;

        super.onDetach();
    }

    public void initViews(View v) {

        isAdClosed = false;
        isReplayPressed = false;

        best = v.findViewById(R.id.best);
        best.setText(String.valueOf(sharedPreferencesManager.getSomeStringValue()));

        points = v.findViewById(R.id.points);
        replay = v.findViewById(R.id.replay_btn);
        home = v.findViewById(R.id.home_btn);
        progressBar = v.findViewById(R.id.progress_indicator_game_over);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.btn_sound);
        replay.setOnClickListener(this);
        home.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        mMediaPlayer.start();

        if(v.getId() == R.id.replay_btn){

            isReplayPressed = true;

            rewardItem = null;

            if(adMobHandler.isRewardedLoaded()){

                showProgressIndi();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // no null check? even you in callback with delay.
                        adMobHandler.showRewardedAd();

                    }
                },500);

            }else{
                returnToGameScreen();
            }

        }else if(v.getId() == R.id.home_btn ){
                returnToHomeMenu(); }
    }

    private void returnToGameScreen(){
        // Intent to Game Screen from this screen
        FactoryClass.moveToPreviousScreen(getFragmentManager(),-1);
    }

    /**
     * what if we add more screen in between for that you will change the count?
     */
    private void returnToHomeMenu(){

        if(getFragmentManager().getBackStackEntryCount() == 2){
            FactoryClass.moveToNextScreen(getActivity(),null,Constants.HOMESCREEN_TAG);
        } else if (getFragmentManager().getBackStackEntryCount() > 2){
            // Intent to Home Screen frm this screen
            FactoryClass.moveToPreviousScreen(getFragmentManager(),0);
        }
    }

    private void hideProgressIndi(){

        if(progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    private void showProgressIndi(){

        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRewardAddOpen() {

        isAdClosed = false;
        hideProgressIndi();
    }

    @Override
    public void onRewardAddClose() {

        isAdClosed = true;

        if(rewardItem != null){

            sharedPreferencesManager.saveReplay(Integer.parseInt(currentScore));
            returnToGameScreen();
        }
        else
            showToast("Please watch complete ad to replay");

    }

    @Override
    public void onRewardAddEarnedItem(RewardItem rewardItem) {

        this.rewardItem = rewardItem;
        showToast("Congrats. You get rewarded with " + rewardItem.getAmount() + "points");
    }

    @Override
    public void onRewardAddFailedtoLoad(AdError error) {

        hideProgressIndi();
        returnToGameScreen();

        showLogs("on Reward Failed to Load");
    }

    private void showLogs(String log){
        Log.d(TAG,log);
    }

    @Override
    public void onLoadFailed() {

        showLogs("on Failed to Load");

        if(isReplayPressed){

            hideProgressIndi();
            returnToGameScreen();
        }

    }

    private void showToast(String msg){

        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(isReplayPressed){

            showLogs("On Pause Called");
            getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }

    }


    @Override
    public void onLoadCompleted() {

    }
}
