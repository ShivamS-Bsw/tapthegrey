package com.example.bsw_firsttask;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class MediaHandler {

    private static MediaHandler mediaHandler;
    private static SoundPool soundPool;
    private static int sound1, sound2;

    synchronized public static MediaHandler getInstance(Context context) {

        if (mediaHandler == null) {
            mediaHandler = new MediaHandler();
        }else if(soundPool == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(2)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            }

            sound1 = soundPool.load(context,R.raw.btn_sound,1);
            sound2 = soundPool.load(context,R.raw.fail,1);
        }
        return mediaHandler;
    }
    public void playOnButtonClick(){

        if(soundPool != null)
            soundPool.play(sound1,1,1,0,0,1);
    }

    public void playOnGameOver(){

        if(soundPool != null) {
            soundPool.play(sound2, 1, 1, 0, 0, 1);
        }
    }

    public void destroySoundPool(){

        if(soundPool != null)
            soundPool.release();

        soundPool = null;
    }
}
