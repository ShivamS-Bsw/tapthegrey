package com.example.bsw_firsttask.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.AdMob.AdMobHandler;
import com.example.bsw_firsttask.Dialogs.CustomDialog;
import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.Utils.FirebaseAnalyticsHelper;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;

public class HomeScreen extends Fragment implements CustomDialog.DialogListener {

    private ImageButton playButton;
    private Button locale;
    private SharedPreferencesManager preferencesManager;
    private Animation scaleAnimation;
    private CustomDialog dialog;
    private static final String TAG = HomeScreen.class.getSimpleName();
    private AdMobHandler adMobHandler;
    public static FrameLayout progressLoader;
    private Button crash;
    private ConstraintLayout home;
    private Bundle params;
    private TextView tap_Text;

    public HomeScreen(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        home = view.findViewById(R.id.home_parent);
        params = new Bundle();

        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        scaleAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.scale_animation);


        initView(view);
        setBackground();

        return view;
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void continueGame(){

        if(preferencesManager.checkSavedGame()){
            showAlertDialog();

        }else {
            newGame();
        }

    }

    private void initView(View view){

        locale = view.findViewById(R.id.locale);
        playButton = view.findViewById(R.id.btnPlay);
        progressLoader = view.findViewById(R.id.progress_indicator_homescreen);
        tap_Text = view.findViewById(R.id.textView2);
    }

    public static void showLoader(){

        if(progressLoader!=null)
            progressLoader.setVisibility(View.VISIBLE);
    }
    public static void hideLoader(){

        if(progressLoader!=null)
                progressLoader.setVisibility(View.GONE);
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

        if(progressLoader.getVisibility() == View.VISIBLE && getActivity() != null)
            getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(scaleAnimation != null)
            scaleAnimation.cancel();
    }

    private void showAlertDialog(){

        dialog = new CustomDialog(getContext(),R.string.continue_game);
        dialog.setTargetFragment(HomeScreen.this,1);

        if(getFragmentManager() != null)
            dialog.show(getFragmentManager(),"custom_dialog");

        dialog.setRetainInstance(true);

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
        //Close the dialog
        dialog.dismiss();
    }

    private void automateScreen(){

        // 1. Press the Play Button
        playButton.performClick();
    }

    public void onBackPressed(){

        ((MainActivity)getActivity()).finishActivity();
    }
}