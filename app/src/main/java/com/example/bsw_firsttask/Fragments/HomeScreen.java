package com.example.bsw_firsttask.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.AdMob.AdMobHandler;
import com.example.bsw_firsttask.Callbacks.ExitInterstitialAdCallback;
import com.example.bsw_firsttask.Dialogs.CustomDialog;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Dialogs.ExitDialog;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.Utils.FirebaseAnalyticsHelper;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;

import java.util.Calendar;

public class HomeScreen extends Fragment implements CustomDialog.DialogListener, ExitInterstitialAdCallback,ExitDialog.ExitDialogListener{

    private ImageButton playButton;
    private Button locale;
    private SharedPreferencesManager preferencesManager;
    private Animation scaleAnimation;
    private CustomDialog continueDialog;
    private ExitDialog exitDialog;
    private static final String TAG = HomeScreen.class.getSimpleName();
    private Bundle params;
    private TextView tap_Text , t1,t2;
    private ProgressBar progressBar;

    @Override
    public void onAdClosed() {
        hideLoader();
        showExitDialog();
    }

    private ExitInterstitialAdCallback adCallback;
    public HomeScreen(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        params = new Bundle();

        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        scaleAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.scale_animation);

        AdMobHandler.getInstance(getActivity()).setExitInterstitialAdCallback(adCallback);
        AdMobHandler.getInstance(getActivity()).showBannerAd();

        initView(view);
        setBackground();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            adCallback = this;
        }catch (ClassCastException e){
            System.out.print(e.getMessage());
        }
    }

    private void setBackground(){

        if(preferencesManager.getBG() == 0 ){
            tap_Text.setTextColor(getResources().getColor(R.color.black));
        }
        else
            tap_Text.setTextColor(getResources().getColor(R.color.white));

    }
    private void showLogs(String instance_not_null) {

        Log.d(TAG,instance_not_null);
    }
    private boolean showLoader(){

        if(progressBar != null ){

            progressBar.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void hideLoader(){

        if(progressBar != null )
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void continueGame(){

        if(preferencesManager.checkSavedGame()){
            showContinueDialog(R.string.continue_game);

        }else {
            newGame();
        }

    }

    private void initView(View view){

        locale = view.findViewById(R.id.locale);
        playButton = view.findViewById(R.id.btnPlay);
        tap_Text = view.findViewById(R.id.textView2);
        progressBar = view.findViewById(R.id.ad_loader);
        t1 = view.findViewById(R.id.t1);
        t2 = view.findViewById(R.id.t2);

        t1.setText(String.format(getString(R.string.d_hello_world),2));
        t2.setText(String.format(getString(R.string.i_m_d_chitty),2));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MediaHandler.getInstance(getContext()).playOnButtonClick();
                continueGame();
            }
        });
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }

    @Override
    public void onResume() {

        playButton.setAnimation(scaleAnimation);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(scaleAnimation != null)
            scaleAnimation.cancel();
    }

    private void showContinueDialog(int titleId){

        continueDialog = new CustomDialog(getContext(),titleId);
        continueDialog.setTargetFragment(HomeScreen.this,1);

        if(getFragmentManager() != null)
            continueDialog.show(getFragmentManager(),"custom_dialog");

        continueDialog.setRetainInstance(true);

    }

    private void newGame(){

        if(getActivity() != null)
            FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.GAMESCREEN_TAG,null,true);
    }

    private void showChangeLanguageDialog(){

        final String[] listItems = {"English","हिन्दी","عربى"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        int checkedItem = getCheckedItemIndex(preferencesManager.getLocale());

        builder.setTitle("Choose Language....");
        builder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                if(i == 0){
                    ((MainActivity)getActivity()).setLocale("en");
                }else if(i == 1) {
                    ((MainActivity)getActivity()).setLocale("hi");
                }else if (i == 2){
                    ((MainActivity)getActivity()).setLocale("ar");
                }

                if(getFragmentManager()!=null && getActivity() != null)
                    getFragmentManager().beginTransaction().detach(HomeScreen.this).attach(HomeScreen.this).commit();

                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int getCheckedItemIndex(String locale) {

        switch (locale){

            case "en":
                return 0;
            case "hi":
                return 1;
            case "ar":
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public void positive() {

            params.putBoolean("is_saved_game",true);
            FirebaseAnalyticsHelper.logCustomEvents(getContext(),Constants.EVENT_1,params);

            // Continue with the saved game and pass the score.
            if(getActivity() != null)
                FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.GAMESCREEN_TAG,null,true);

    }

    @Override
    public void negative() {

            params.putBoolean("is_saved_game",false);
            FirebaseAnalyticsHelper.logCustomEvents(getContext(),Constants.EVENT_1,params);

            //Clear the Shared Preference and start the new game
            if(preferencesManager != null && getActivity() != null )
                preferencesManager.clearSavedGame();

            newGame();
    }

    @Override
    public void close() {
        //Close the continueDialog
        continueDialog.dismiss();
    }
//
//    private void automateScreen(){
//
//        // 1. Press the Play Button
//        playButton.performClick();
//    }
//
    private void showExitDialog(){

        exitDialog = new ExitDialog(getContext(),R.string.exit);
        exitDialog.setTargetFragment(HomeScreen.this,2);

        if(getFragmentManager() != null)
            exitDialog.show(getFragmentManager(),"custom_dialog");

        exitDialog.setRetainInstance(true);

    }
    public void onBackPressed(){

        if( !showExitInterstitialAd()){
            showExitDialog();
        }
    }

    private boolean showExitInterstitialAd() {

        long lastShown = 0;

        if(getActivity() != null)
            lastShown = ((MainActivity)getActivity()).getLastInterstitialShown();

        return getActivity() != null
                && ((lastShown == 0 || (Calendar.getInstance().getTimeInMillis() - lastShown) >= 60000))
                && ((MainActivity) getActivity()).showExitInterstitialAd()
                && showLoader();
    }

    @Override
    public void exit() {

        if(getActivity() != null ) {
            ((MainActivity)getActivity()).finishActivity();
        }
    }

    @Override
    public void dontExit() {

        if(exitDialog != null)
            exitDialog.dismiss();
    }
}