package com.example.bsw_firsttask.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class SharedPreferencesManager {

    private static SharedPreferencesManager sharePref = new SharedPreferencesManager();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static final String PREF_NAME = "game_pref";

    // properties
    private static final String KEY_BEST = "best_score";
    private static final String IS_GAME_SAVED = "isGameSaved";
    private static final String SAVED_SCORE = "saved_score";
    private static final String LOCALE = "locale";
    private static final String IS_REPLAYED = "isReplayed";
    private static final String REPLAY_GAME_SCORE = "replay_game_score";
    private static final String IS_STATE_SAVED = "state_saved";
    private static final String STATE_SCORE = "state_score";

    private static final String Count = "count";
    private static final String PREVIOUS_SCORE = "previous_score";


    private SharedPreferencesManager() {}

    public static SharedPreferencesManager getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(),MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return sharePref;
    }

    public int getGamerCount(){
        return sharedPreferences.getInt(Count,0);
    }
    public int getPreviousScore(){
        return sharedPreferences.getInt(PREVIOUS_SCORE,0);
    }

    public void setGamerCount(int count){
        editor.putInt(Count,count);
        editor.commit();
    }
    public void setPreviousScore(int score){
        editor.putInt(PREVIOUS_SCORE,score);
        editor.commit();
    }

    public void saveLocale(String locale){

        editor.putString(LOCALE,locale);
        editor.commit();
    }

    public String getLocale(){
        return sharedPreferences.getString(LOCALE,"en");
    }
    public int getBestScore() {
        return sharedPreferences.getInt(KEY_BEST , 0);
    }

    public  void setBestScore(int newValue) {
        editor.putInt(KEY_BEST , newValue);
        editor.commit();
    }
    public void clearAll() {
        editor.clear();
        editor.commit();
    }

    public void saveGame(int score){

        editor.putInt(SAVED_SCORE , score);
        editor.putBoolean(IS_GAME_SAVED, true);

        editor.commit();
    }

    public int getReplayScore(){
        return sharedPreferences.getInt(REPLAY_GAME_SCORE,-1);
    }

    public boolean checkIsReplayed(){

        return sharedPreferences.getBoolean(IS_REPLAYED,false);
    }

    public void saveReplay(int scoreCount){

        editor.putInt(REPLAY_GAME_SCORE , scoreCount);
        editor.putBoolean(IS_REPLAYED, true);

        editor.commit();

    }
    public void clearReplayGame(){

        editor.remove(REPLAY_GAME_SCORE);
        editor.remove(IS_REPLAYED);

        editor.commit();
    }
    public void clearSavedGame(){

        editor.remove(SAVED_SCORE);
        editor.remove(IS_GAME_SAVED);

        editor.commit();
    }
    public boolean checkSavedGame(){
        return sharedPreferences.getBoolean(IS_GAME_SAVED, false);
    }

    public int getSavedScore(){
        return sharedPreferences.getInt(SAVED_SCORE,0);
    }

    public void clearGamer() {

        editor.remove(Count);
        editor.remove(PREVIOUS_SCORE);

        editor.commit();
    }
}
