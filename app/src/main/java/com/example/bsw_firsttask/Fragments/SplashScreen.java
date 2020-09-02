package com.example.bsw_firsttask.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.FactoryClass;
import com.example.bsw_firsttask.R;
import com.example.bsw_firsttask.SharedPref.SharedPreferencesManager;

public class SplashScreen extends Fragment {

    private SharedPreferencesManager preferencesManager;

    public SplashScreen(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash,container,false);
        preferencesManager = SharedPreferencesManager.getInstance(getContext());
        return view;
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

                    FactoryClass.moveToNextScreen(getActivity(),params,Constants.GAMESCREEN_TAG);

                }else{

                    FactoryClass.moveToNextScreen(getActivity(),null,Constants.HOMESCREEN_TAG);
                }
            }
        }, Constants.SPLASH_DELAY);
    }

}
