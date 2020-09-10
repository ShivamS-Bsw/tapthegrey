package com.example.bsw_firsttask.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Dialogs.CustomDialog;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Utils.FirebaseAnalyticsHelper;
import com.example.bsw_firsttask.Utils.Mail;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;


import java.util.Random;

// no on pause onStop Override? when you stop your Handler?
public class GameScreen extends Fragment implements View.OnClickListener , CustomDialog.DialogListener , CustomDialog.DialogLifecycleListener {

    public static final String TAG = GameScreen.class.getSimpleName();
    private TextView scoreTextView,timer;
    private Button button1,button2,button3,button4,support;
    private Random rand;
    private int currentButton, lastButton;
    private Handler handler;
    private boolean mStopHandler;
    private boolean isButtonClicked;
    private MediaHandler mediaHandler;
    private Runnable runnable;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    private SharedPreferencesManager preferencesManager;

    private Context context;
    private Bundle params1,params2,params3;

    int green = Color.parseColor("#17b978");
    int blue = Color.parseColor("#248bcc");
    int yellow = Color.parseColor("#fef200");
    int red = Color.parseColor("#ffa010");
    int grey = Color.parseColor("#cdd5d5");

    private int scoreCount;
    private boolean allowButtonCLick;
    private CustomDialog dialog;
    private boolean dialogResumed = false;
    private boolean dialogClosed = true;
    private boolean currentFragment = true;
    private long mLastClickTime = 0;
    private int maxCountdownTime, gameTime;

    public GameScreen(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen,container,false);

        initViews(view);

        Log.i(TAG,"On Create");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initClasses();
        scoreCount = 0;

        //Get the score count if the fragment is destroyed and recreated
        if(savedInstanceState != null){
            scoreCount = savedInstanceState.getInt(Constants.STATE_SCORE);
        }


        setButtonListeners();

        lastButton = rand.nextInt(4)+1;
        context = getContext();

        if(preferencesManager != null ){

            maxCountdownTime = preferencesManager.getGameStartTime();
            gameTime = preferencesManager.getGameTime();
        }

        if(preferencesManager.checkSavedGame())
            scoreCount = preferencesManager.getSavedScore();

        // Only when in rewarded ad gets rewarded
        if(preferencesManager.checkIsReplayed()){

            //Get the replay score and set the score count to replay score
            scoreCount = preferencesManager.getReplayScore();

            //Clear the current replay score
            preferencesManager.clearReplayGame();
        }


        scoreTextView.setText(String.valueOf(scoreCount));
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;

                }
                mLastClickTime = SystemClock.elapsedRealtime();
                email();

            }
        });
    }

    private void email(){

        pauseHandler();
        Mail.getInstance(getActivity()).sendEmail(scoreCount);

    }
    private void initClasses(){

        mediaHandler = MediaHandler.getInstance(getContext());
        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        handler = new Handler();
        rand = new Random();
        params1 = new Bundle(); // Event 3 =Game Left
        params2 = new Bundle(); // Event 4 = Gamae End
        params3 =  new Bundle(); // Event 2 = Game SAved

    }
    private void setButtonListeners() {
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
    }
    private void initViews(View view){

        scoreTextView  = view.findViewById(R.id.score);
        button1= view.findViewById(R.id.button00);
        button2= view.findViewById(R.id.button01);
        button3= view.findViewById(R.id.button10);
        button4= view.findViewById(R.id.button11);
        timer = view.findViewById(R.id.countdown);
        support = view.findViewById(R.id.button_support);
    }

    private void startCountDown(){

        timer.setVisibility(View.VISIBLE);
        new CountDownTimer(maxCountdownTime,1000){

            @Override
            public void onTick(long millisUntilFinished) {

                if((millisUntilFinished/1000) == 0)
                    timer.setText("GO");
                else
                    timer.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {

                timer.setVisibility(View.GONE);

                if(getActivity() != null)
                    startMatch();
            }
        }.start();

    }

    private void startMatch(){

         runnable = new Runnable() {
            @Override
            public void run() {

                // if button not clicked within 1seconds
                if(!isButtonClicked && !mStopHandler){

                    gameOver(scoreCount);
                    scoreTextView.setText(String.valueOf(scoreCount));
                    mStopHandler = true;
                    return;
                }

                if(!mStopHandler && getActivity() != null ) {

                    allowButtonCLick = true;
                    currentButton = rand.nextInt(4) + 1;

                    // If previous and current box are same, then try for some other box
                    if(currentButton == lastButton)
                        currentButton = ((currentButton+1)%4 )== 0 ? 1 :(currentButton+1)%4;

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
                        handler.postDelayed(this,gameTime);
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private void gameOver(int currentScore){

        params2.putInt("score_val",currentScore);
        FirebaseAnalyticsHelper.logCustomEvents(getContext(),Constants.EVENT_4,params2);

        gamer(currentScore);

        mediaHandler.playOnGameOver();

        //Move to Game Over Screen and pass the params i.e current score
        Bundle args = new Bundle();
        args.putInt(Constants.CURRENT_SCORE,currentScore);

        if(getActivity() != null)
            FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.GAMEOVERCREEN_TAG,args,true);
    }

    private void gamer(int currentScore){

        if(currentScore < preferencesManager.getPreviousScore())
            preferencesManager.clearGamer();

        if(preferencesManager.getGamerCount() == 3 ){// This is the 3rd time

            if(preferencesManager.getPreviousScore()< currentScore){

                Toast.makeText(getContext(),"Yes!! I'm a Gamer",Toast.LENGTH_SHORT).show();
                FirebaseAnalyticsHelper.setUserProperty(context,Constants.USER_PROPERTY_2,"Gamer");
            }// Else he is not a gamer
        }
    }

    @Override
    public void onClick(View v) {

        if(allowButtonCLick){

            boolean increment = checkButtonClick(v.getId(),currentButton);
            if (increment){
                mediaHandler.playOnButtonClick();
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
    public void onResume() {
        super.onResume();

        showLogs("On Resume Called");

        currentFragment = true;

        if(!dialogResumed && dialogClosed ){

            isButtonClicked = true;
            allowButtonCLick = false;
            mStopHandler = false;

            startCountDown();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        showLogs("On Pause Called");

        pauseHandler();
    }
    @Override
    public void onStop() {
        super.onStop();

        showLogs("On Stop Called");
        //this means fragment is not anymore visible
        //stop the game

        currentFragment = false;

        if(dialog != null && dialogResumed)
            pauseHandler();
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

    private void pauseHandler(){

        mStopHandler = true;

        if(handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    private void restartHandler(){

        isButtonClicked = true;
        allowButtonCLick = false;
        mStopHandler = false;
        startCountDown();
    }

    public void showDialog(){

        if(dialog == null){

            dialog = new CustomDialog(getContext(),R.string.save_game);
            dialog.setTargetFragment(GameScreen.this,1);

        }

        if(getFragmentManager() != null ){

            dialogResumed = false;
            dialog.show(getFragmentManager(),"custom_dialog");
        }

        dialog.setRetainInstance(true);
        dialog.setCancelable(false);
    }


    // Method is called when the system abruptly destroys the application. It is called just after onStop();
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Constants.STATE_SCORE,scoreCount);
    }


    // Dialogs positive and negative click methods
    @Override
    public void positive() {
        // 1. Save the score in shared preference and set the saved flag to YES

        params3.putInt("score",scoreCount);
        FirebaseAnalyticsHelper.logCustomEvents(getContext(),Constants.EVENT_2,params3);

        if(preferencesManager != null){
            preferencesManager.saveGame(scoreCount);
        }

        //2. Move to Home Screen
        moveToHomeScreen();
    }

    private void moveToHomeScreen(){

        if(getFragmentManager()!= null && getFragmentManager().findFragmentByTag(Constants.HOMESCREEN_TAG) instanceof HomeScreen){
            FactoryClass.moveToPreviousScreen(getFragmentManager(),null);

        }
        else{

            if(getActivity() != null)
                FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.HOMESCREEN_TAG,null,true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showLogs("On Destroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        showLogs("OnDestroyView");

        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void negative() {

        params1.putInt("score_val",scoreCount);
        FirebaseAnalyticsHelper.logCustomEvents(getContext(),Constants.EVENT_3,params1);

        if(preferencesManager != null)
            preferencesManager.clearSavedGame();

        moveToHomeScreen();
    }

    @Override
    public void close() {
    }

    @Override
    public void onDismiss() {

        dialogResumed = false;
        dialogClosed = true;
    }

    //For Game loop testing
    private void automateGame(int currentButton){

        if(scoreCount != 5 ){
            performClick(currentButton);
        }
    }

    private void showLogs(String msg){
        Log.d(TAG,msg);
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

    // Override Dialog Lifecycle Methods
    @Override
    public void OnDialogResume() {

        dialogResumed = true;
        dialogClosed = false;


            if(getActivity() != null && currentFragment) {
                showLogs("On Dialog Resume");
                pauseHandler();
        }
    }

    @Override
    public void OnDialogPause() {


        showLogs("on Dialog Paused");
        // if it is called in same fragment and dialog is closed - Called after onDismiss
        if(getActivity() != null && dialogClosed && currentFragment  ){
            restartHandler();
        }
    }
}
