package com.example.bsw_firsttask.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Factory.FactoryClass;
import com.example.bsw_firsttask.Media.MediaHandler;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;

public class SplashScreen extends Fragment {

    private SharedPreferencesManager preferencesManager;
    private ConstraintLayout splash;
    private TextView textView;
    public SplashScreen(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash,container,false);

        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        textView = view.findViewById(R.id.textView);
        splash = view.findViewById(R.id.splash_parent);

        setBackground();

        return view;
    }

    private void setBackground(){

        if(preferencesManager.getBG() == 0 ){
            textView.setTextColor(getResources().getColor(R.color.black));
        }

        else{
            textView.setTextColor(getResources().getColor(R.color.white));
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Check for any saved game
                if(preferencesManager.checkSavedGame()){

                    // Get the saved score
                    Bundle params = new Bundle();
                    params.putInt(Constants.SAVED_SCORE,preferencesManager.getSavedScore());

                    FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.GAMESCREEN_TAG,params,true);

                }else{
                    FactoryClass.getInstance().moveToNextScreen(getActivity(),Constants.HOMESCREEN_TAG,null,true);
                }
            }
        }, Constants.SPLASH_DELAY);
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
