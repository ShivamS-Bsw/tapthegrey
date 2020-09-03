package com.example.bsw_firsttask;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bsw_firsttask.Activity.MainActivity;
import com.example.bsw_firsttask.Factory.*;
import com.example.bsw_firsttask.Fragments.GameOverScreen;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.Fragments.HomeScreen;

public class FactoryClass {

    public static void moveToNextScreen(FragmentActivity activity,Bundle args, String fragmentTag){

        if(activity!=null){

            Fragment fragment = getFragmentObject(fragmentTag);

            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
            fragment.setArguments(args);

            fragment.setRetainInstance(true);

            fragmentTransaction.replace(R.id.frame_container, fragment,fragmentTag);
            fragmentTransaction.addToBackStack(fragmentTag);

            if(!fragmentManager.isStateSaved()) {
                fragmentTransaction.commit();
            }
        }
    }

    public static Fragment getFragmentObject(String className){

        switch (className){

            case Constants.HOMESCREEN_TAG:
                return new HomeScreen();

            case Constants.GAMESCREEN_TAG:
                return new GameScreen();

            case Constants.GAMEOVERCREEN_TAG:
                return new GameOverScreen();

        }
        return new HomeScreen();
    }

    public static void moveToPreviousScreen(FragmentManager fragmentManager,int id){

        if(fragmentManager != null){
            if(id == -1)
                fragmentManager.popBackStack();
            else
                fragmentManager.popBackStack(id,0);
        }
    }
}
