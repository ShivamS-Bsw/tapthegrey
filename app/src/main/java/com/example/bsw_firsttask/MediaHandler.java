package com.example.bsw_firsttask;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaHandler {

    private static MediaHandler mediaHandler;
    private static MediaPlayer mediaPlayerFail = null, mediaPlayerClick = null;

    synchronized public static MediaHandler getInstance(Context context){

        if(mediaHandler == null){

            mediaPlayerFail = MediaPlayer.create(context,R.raw.fail);
            mediaPlayerClick = MediaPlayer.create(context,R.raw.btn_sound);
            mediaHandler = new MediaHandler();
        }
        return mediaHandler;
    }

    public void playOnButtonClick(){

        if(mediaPlayerClick != null){
            mediaPlayerClick.start();
        }
    }

    public void playOnGameOver(){
        if(mediaPlayerFail != null){
            mediaPlayerFail.start();
        }
    }

}
