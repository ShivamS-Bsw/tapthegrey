package com.example.bsw_firsttask.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.CustomDialog;
import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.FactoryClass;
import com.example.bsw_firsttask.MediaHandler;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;


import java.security.Key;
import java.util.List;
import java.util.Random;

// no on pause onStop Override? when you stop your Handler?
public class GameScreen extends Fragment implements View.OnClickListener , CustomDialog.DialogListener , CustomDialog.DialogLifecycleListener {

    public static final String TAG = GameScreen.class.getSimpleName();
    private TextView scoreTextView;
    private Button button1,button2,button3,button4;
    private Random rand;
    private int currentButton, lastButton;
    private Handler handler;
    private boolean mStopHandler;
    private boolean isButtonClicked;
    private MediaHandler mediaHandler;
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
    private boolean dialogResumed = false;

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

        isButtonClicked = true;
        allowButtonCLick = false;


        setButtonListeners();

        lastButton = rand.nextInt(4)+1;
        mStopHandler = false;
        context = getContext();


        if(preferencesManager.checkSavedGame())
            scoreCount = preferencesManager.getSavedScore();

        if(preferencesManager.checkIsReplayed()){

            //Get the replay score and set the score count to replay score
            scoreCount = preferencesManager.getReplayScore();

            //Clear the current replay score
            preferencesManager.clearReplayGame();
        }
        scoreTextView.setText(String.valueOf(scoreCount));

    }

    private void initClasses(){

        mediaHandler = MediaHandler.getInstance(getContext());
        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        handler = new Handler();
        rand = new Random();

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

    }

    private void startHandler(int delayTime){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(getActivity() != null)
                    startMatch();

            }
        }, delayTime);
    }

    /**
     * no null check in callback?
     *
     */
    private void startMatch(){
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
                        handler.postDelayed(this,1000);
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private void gameOver(int currentScore){

        mediaHandler.playOnGameOver();

        //Move to Game Over Screen and pass the params i.e current score
        Bundle args = new Bundle();
        args.putInt(Constants.CURRENT_SCORE,currentScore);

        if(getActivity() != null)
            FactoryClass.moveToNextScreen(getActivity(),args,Constants.GAMEOVERCREEN_TAG);

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

        // TODO:  Show the 2 timer to user and then start the match
        resumeHandler();
        startHandler(Constants.TIME_IN_MILLISECONDS);

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

    public void pauseHandler(){
        mStopHandler = true;
    }

    public void resumeHandler(){
        mStopHandler = false;
    }

    // from restart handler from custom dialog?
    public void restartHandler(){

        mStopHandler = false;
        startHandler(Constants.TIME_IN_MILLISECONDS);
    }


    public void showDialog(){

        dialog = new CustomDialog(getContext(),R.string.save_game);
        dialog.setTargetFragment(GameScreen.this,1);

        if(getFragmentManager() != null ){

            dialogResumed = false;
            dialog.show(getFragmentManager(),"custom_dialog");
        }

        dialog.setCancelable(false);
    }


    // Method is called when the system abruptly destroys the application. It is called just after onStop();
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Constants.STATE_SCORE,scoreCount);

//        // Not Sure about this need to ask
//        if(preferencesManager != null)
//            preferencesManager.saveStateScore(scoreCount);

    }


    // Dialogs positive and negative click methods
    @Override
    public void positive() {

        // 1. Save the score in shared preference and set the saved flag to YES

        if(preferencesManager != null)
            preferencesManager.saveGame(scoreCount);
        //2. Move to Home Screen
        moveToHomeScreen();
    }

    private void moveToHomeScreen(){

        if(getFragmentManager()!= null && getFragmentManager().findFragmentByTag(Constants.HOMESCREEN_TAG) instanceof HomeScreen){
            FactoryClass.moveToPreviousScreen(getFragmentManager(),-1);
        }else{

            if(getActivity() != null)
                FactoryClass.moveToNextScreen(getActivity(),null,Constants.HOMESCREEN_TAG);
        }
    }

    @Override
    public void negative() {

        //Clear the saved game

        if(preferencesManager != null)
            preferencesManager.clearSavedGame();

        moveToHomeScreen();
    }

    @Override
    public void close() {

        Toast.makeText(getContext(),"Press the Grey Color to Resume",Toast.LENGTH_LONG).show();
        dialog.dismiss();

    }
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

        showLogs("On Dialog Resume");
        if(getActivity() != null){
            pauseHandler();
        }

        if(dialog != null)
            dialog.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                    if(keyCode == KeyEvent.KEYCODE_BACK){

                        Toast.makeText(getContext(),"Press the Grey Color to Resume",Toast.LENGTH_LONG).show();
                        dialog.dismiss();

                        return true;
                    }

                    return false;
                }
            });
    }

    @Override
    public void OnDialogPause() {
        showLogs("on Dialog Paused");
        if(getActivity() != null)
            restartHandler();
    }
}
