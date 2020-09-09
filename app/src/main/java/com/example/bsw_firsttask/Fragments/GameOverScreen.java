package com.example.bsw_firsttask.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.AdMob.AdMobHandler;
import com.example.bsw_firsttask.Callbacks.RewardAdCallbacks;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Utils.FirebaseAnalyticsHelper;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class GameOverScreen extends Fragment implements View.OnClickListener,RewardAdCallbacks {

    private MediaHandler mediaHandler;
    public static final String TAG = GameOverScreen.class.getSimpleName();
    private TextView points,best;
    private Button replay,home;
    private SharedPreferencesManager sharedPreferencesManager;

    private RewardAdCallbacks rewardedAdCallback;

    private boolean isRewardAdRequested;
    private RewardItem rewardItem;
    private int currentScore;

    private FrameLayout progressBar;
    public GameOverScreen(){ }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_over,container,false);

        initViews(view);
        return view;
    }

    private void initClasses(){

        sharedPreferencesManager = SharedPreferencesManager.getInstance(getContext());
        mediaHandler = MediaHandler.getInstance(getContext());

        AdMobHandler.getInstance(getActivity()).setRewardedAdCallback(rewardedAdCallback);
    }

    private void initListeners(){

        if(replay != null)
            replay.setOnClickListener(this);
        if(home != null)
            home.setOnClickListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


        try {

            rewardedAdCallback = this;

        }catch (ClassCastException e){
            System.out.print(e.getMessage());
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        rewardedAdCallback = null;
    }

    private void initViews(View v) {

        isRewardAdRequested = false;

        best = v.findViewById(R.id.best);
        points = v.findViewById(R.id.points);
        replay = v.findViewById(R.id.replay_btn);
        home = v.findViewById(R.id.home_btn);
        progressBar = v.findViewById(R.id.progress_indicator_game_over);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initClasses();
        initListeners();

        best.setText(String.valueOf(sharedPreferencesManager.getBestScore()));
        currentScore = getArguments().getInt(Constants.CURRENT_SCORE,-1);

        if(sharedPreferencesManager.getGamerCount() < 3){

            sharedPreferencesManager.setPreviousScore(currentScore);
            sharedPreferencesManager.setGamerCount(sharedPreferencesManager.getGamerCount() + 1);
        }
        else
            sharedPreferencesManager.clearGamer();

        if(savedInstanceState != null)
            currentScore = savedInstanceState.getInt(Constants.STATE_SCORE);


        clearSavedScore();
        clearReplayScore();

        points.setText(String.valueOf(currentScore));

        if(currentScore >= sharedPreferencesManager.getBestScore()){

            sharedPreferencesManager.setBestScore(currentScore);
            best.setText(String.valueOf(sharedPreferencesManager.getBestScore()));

            FirebaseAnalyticsHelper.setUserProperty(getContext(),Constants.USER_PROPERTY_1,String.valueOf(currentScore));

        }

    }

    private void clearReplayScore() {

        //Clear the replay game
        if(sharedPreferencesManager != null  && sharedPreferencesManager.checkIsReplayed())
            sharedPreferencesManager.clearReplayGame();

    }
    private void clearSavedScore(){
        //Clear the saved game
        if(sharedPreferencesManager != null && sharedPreferencesManager.checkSavedGame())
            sharedPreferencesManager.clearSavedGame();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        showLogs("On Saved Instance called");

        outState.putInt(Constants.STATE_SCORE,currentScore);
    }

    @Override
    public void onClick(View v) {

        mediaHandler.playOnButtonClick();

        if(v.getId() == R.id.replay_btn){

            rewardItem = null;

            if(AdMobHandler.getInstance(getActivity()).showRewardedAd()){
                isRewardAdRequested = true;
                showProgressIndicator();

            }else{

                isRewardAdRequested = false;
                returnToGameScreen();
            }

        }else if(v.getId() == R.id.home_btn ){
            returnToHomeMenu();
        }
    }

    private void returnToGameScreen(){
        FactoryClass.moveToPreviousScreen(getFragmentManager(),null);
    }

    public void returnToHomeMenu(){

        showLogs("Return to Home Menu");

        if(getFragmentManager()!= null && getFragmentManager().findFragmentByTag(Constants.HOMESCREEN_TAG) instanceof HomeScreen)
            FactoryClass.moveToPreviousScreen(getFragmentManager(),Constants.HOMESCREEN_TAG);

        else if(getActivity() != null)
            FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.HOMESCREEN_TAG,null,true);
    }

    private void hideProgressIndicator(){

        if(progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    private void showProgressIndicator(){

        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRewardAddOpen() {
        hideProgressIndicator();
    }

    @Override
    public void onRewardAddClose() {

        if(rewardItem != null){
            sharedPreferencesManager.saveReplay(currentScore);
        }

        returnToGameScreen();
    }

    @Override
    public void onRewardAddEarnedItem(RewardItem rewardItem) {

        this.rewardItem = rewardItem;
        showToast("Congrats. You get rewarded with " + rewardItem.getAmount() + "points");
    }

    @Override
    public void onRewardAddFailedtoLoad(AdError error) {

        hideProgressIndicator();
        returnToGameScreen();

        showLogs("on Reward Failed to Load");
    }

    private void showLogs(String log){
        Log.d(TAG,log);
    }

    private void showToast(String msg){
        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(isRewardAdRequested){

            showLogs("On Pause Called");
            getActivity().overridePendingTransition(android.R.anim.slide_in_left,0);
        }
    }
}
