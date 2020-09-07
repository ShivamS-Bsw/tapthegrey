package com.example.bsw_firsttask.Factory;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bsw_firsttask.Constants.Constants;
import com.example.bsw_firsttask.Fragments.GameOverScreen;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.Fragments.HomeScreen;
import com.example.bsw_firsttask.Fragments.SplashScreen;
import com.example.bsw_firsttask.R;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;

public class FactoryClass {


    private static FactoryClass instance;

    private FactoryClass() {
    }

    public static FactoryClass getInstance(){
        return instance == null ? new FactoryClass() : instance;
    }

    public void moveToNextScreen(FragmentActivity activity,String fragmentTag,Bundle args, boolean addToBackStack){

        if(activity!=null){

            Fragment fragment = getFragmentObject(fragmentTag);

            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);

            if(args != null)
            fragment.setArguments(args);

            fragmentTransaction.replace(R.id.frame_container, fragment,fragmentTag);

            if(addToBackStack)
                fragmentTransaction.addToBackStack(fragmentTag);

            if(!fragmentManager.isStateSaved()) {
                fragmentTransaction.commit();
            }
        }
    }

    public static Fragment getFragmentObject(String className){

        switch (className){

            case Constants.SPLASHSCREEN_TAG:
                return new SplashScreen();

            case Constants.HOMESCREEN_TAG:
                return new HomeScreen();

            case Constants.GAMESCREEN_TAG:
                return new GameScreen();

            case Constants.GAMEOVERCREEN_TAG:
                return new GameOverScreen();

        }
        return new HomeScreen();


    }

    public static void moveToPreviousScreen(FragmentManager fragmentManager,String name){

        if(fragmentManager != null ){

            // If moves to a particular fragment
            if(name != null)
                fragmentManager.popBackStack(name,0); // Remove the all fragment transaction above the @named fragment excluding it
            else
                fragmentManager.popBackStack();

        }
    }

}
