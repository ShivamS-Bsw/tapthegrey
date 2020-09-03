package com.example.bsw_firsttask.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Rating;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.AdMobHandler;
import com.example.bsw_firsttask.Callbacks.HomeInterstitialAdCallback;
import com.example.bsw_firsttask.CustomDialog;
import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.FactoryClass;
import com.example.bsw_firsttask.NetworkReceiver;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;


import java.util.Random;

public class GameScreen extends Fragment implements View.OnClickListener , CustomDialog.DialogListener {

    public static final String TAG = GameScreen.class.getSimpleName();
    private TextView scoreTextView;
    private Button button1,button2,button3,button4;
    private Random rand;
    private int currentButton, lastButton;
    private Handler handler;
    private boolean mStopHandler;

    private MediaPlayer mMediaPlayer;
    private MediaPlayer mMediaPlayerFail;
    private boolean isButtonClicked;
    private SharedPreferencesManager preferencesManager;

    private Context context;

    int green = Color.parseColor("#17b978");
    int blue = Color.parseColor("#248bcc");
    int yellow = Color.parseColor("#fef200");
    int red = Color.parseColor("#ffa010");
    int grey = Color.parseColor("#cdd5d5");


    private int scoreCount;
    private boolean allowButtonCLick;
    private CustomDialog dialog;

    public GameScreen(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen,container,false);
        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        initViews(view);

        Log.i(TAG,"On Create");


        isButtonClicked = true;
        allowButtonCLick = false;
        scoreCount =  0;

        if(preferencesManager.checkSavedGame())
            scoreCount = preferencesManager.getSavedScore();

        if(preferencesManager.checkIsReplayed()){

            //Get the replay score and set the score count to replay score
            scoreCount = preferencesManager.getReplayScore();
            Log.d(TAG,"Game Replayed" + scoreCount);

            //Clear the current replay score
            preferencesManager.clearReplayGame();
        }

        scoreTextView.setText(String.valueOf(scoreCount));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i(TAG,"On Activity Created");

        mMediaPlayerFail = MediaPlayer.create(getContext(), R.raw.fail);
        handler = new Handler();
        rand = new Random();

        lastButton = rand.nextInt(4)+1;

        button1.setOnClickListener(this);

        button2.setOnClickListener(this);

        button3.setOnClickListener(this);

        button4.setOnClickListener(this);

        mStopHandler = false;

        context = getContext();
        startHandler(Constants.TIME_IN_MILLISECONDS);

    }

    private void startHandler(int delayTime){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startMatch();
            }
        }, delayTime);
    }
    private void initViews(View view){

        scoreTextView  = view.findViewById(R.id.score);

        button1= view.findViewById(R.id.button00);
        button2= view.findViewById(R.id.button01);
        button3= view.findViewById(R.id.button10);
        button4= view.findViewById(R.id.button11);

    }

    private void startMatch(){

        Log.i(TAG,"Match Started");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // if button not clicked within 1seconds
                if(!isButtonClicked && !mStopHandler){

                    gameOver(scoreCount);
                    scoreTextView.setText(String.valueOf(scoreCount));
                    mStopHandler = true;
                    return;
                }

                if(!mStopHandler && isAdded() ) {

                    allowButtonCLick = true;
                    currentButton = rand.nextInt(4) + 1;

                    switch (lastButton) {
                        case 1:
                            button1.setBackgroundColor(red);
                            break;
                        case 2:
                            button2.setBackgroundColor(blue);
                            break;
                        case 3:
                            button3.setBackgroundColor(yellow);
                            break;
                        case 4:
                            button4.setBackgroundColor(green);
                            break;
                        default:
                            break;
                    }
                    switch (currentButton) {
                        case 1:
                            button1.setBackgroundColor(grey);
                            break;
                        case 2:
                            button2.setBackgroundColor(grey);
                            break;
                        case 3:
                            button3.setBackgroundColor(grey);
                            break;
                        case 4:
                            button4.setBackgroundColor(grey);
                            break;
                        default:
                            break;
                    }

                    lastButton = currentButton;
                    isButtonClicked = false;

                    // Automate the button Click
                   // automateGame(currentButton);

                    if (!mStopHandler) {
                        handler.postDelayed(this,1000);
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private void gameOver(int currentScore){

        mMediaPlayerFail.start();

        //Move to Game Over Screen and pass the params i.e current score
        Bundle args = new Bundle();
        args.putString(Constants.CURRENT_SCORE,String.valueOf(currentScore));

        FactoryClass.moveToNextScreen(getActivity(),args,Constants.GAMEOVERCREEN_TAG);

    }

    @Override
    public void onClick(View v) {

        if(allowButtonCLick){

            boolean increment = checkButtonClick(v.getId(),currentButton);
            if (increment){

                if(mMediaPlayer != null)
                {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                mMediaPlayer = MediaPlayer.create(getContext(), R.raw.btn_sound);
                mMediaPlayer.start();

                scoreCount++;
            }

            else{

                gameOver(scoreCount);
                scoreCount = 0;
                mStopHandler = true;

                return;
            }

            isButtonClicked = true;
            scoreTextView.setText(String.valueOf(scoreCount));
        }

        allowButtonCLick = false;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private boolean checkButtonClick(int buttonId, int currentButton){

        if(buttonId == R.id.button00 && currentButton == 1){
            return true;
        }
        else if(buttonId == R.id.button01 && currentButton == 2 ){
            return true;
        }
        else if(buttonId == R.id.button10 && currentButton == 3 ){
            return true;
        }
        else if(buttonId == R.id.button11 && currentButton == 4 ){
            return true;
        }
        else
            return false;
    }

    public void stopHandler(){
        mStopHandler = true;
        showDialog();
    }
    public void restartHandler(){

        mStopHandler = false;
        startHandler(Constants.RESTART_TIME);
    }


    public void showDialog(){

        dialog = new CustomDialog(getContext(),R.string.save_game);
        dialog.setTargetFragment(GameScreen.this,1);

        if(getFragmentManager() != null)
            dialog.show(getFragmentManager(),"custom_dialog");

        dialog.setCancelable(false);
    }


    // Method is called when the system abruptly destroys the application. It is called just after onStop();
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putInt(Constants.STATE_SCORE,scoreCount);
        super.onSaveInstanceState(outState);

    }

    // Method is used to restore the saved instance. It is called just after onStart();
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

        if(savedInstanceState != null)
            scoreCount = savedInstanceState.getInt(Constants.STATE_SCORE);

        super.onViewStateRestored(savedInstanceState);

    }

    // Dialogs positive and negative click methods
    @Override
    public void positive() {

        // 1. Save the score in shared preference and set the saved flag to YES
        preferencesManager.saveGame(scoreCount);
        //2. Move to Home Screen

        if(getFragmentManager().getBackStackEntryCount() < 2){
            FactoryClass.moveToNextScreen(getActivity(),null,Constants.HOMESCREEN_TAG);
        }
        else {
            // Don't save the match and move to home screen
            FactoryClass.moveToPreviousScreen(getFragmentManager(),-1);
        }

    }

    @Override
    public void negative() {

        //Clear the saved game
        preferencesManager.clearSavedGame();

        if(getFragmentManager().getBackStackEntryCount()< 2 ){

            FactoryClass.moveToNextScreen(getActivity(),null,Constants.HOMESCREEN_TAG);
        }
        else {
            // Don't save the match and move to home screen
            FactoryClass.moveToPreviousScreen(getFragmentManager(),-1);
        }

    }

    @Override
    public void close() {

        Toast.makeText(getContext(),"Press the Grey Color to Resume",Toast.LENGTH_LONG).show();

        dialog.dismiss();
        restartHandler();

    }
    private void automateGame(int currentButton){

        if(scoreCount != 5 ){
            performClick(currentButton);
        }
    }

    private void performClick(int currentButton){

        switch (currentButton){

            case 1: button1.performClick();
                break;
            case 2: button2.performClick();
                break;
            case 3: button3.performClick();
                break;
            case 4: button4.performClick();
                break;
        }
    }
}
