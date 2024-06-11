package com.littlefoxstudios.muzify;

import android.content.Context;
import android.media.MediaPlayer;

public class TunePlayer {
    private MediaPlayer player;
    private Context context;
    private int tuneID;

    TunePlayer(Context context, int tuneID){
        stop();
        this.tuneID = tuneID;
        this.context = context;
        initializePlayer();
    }

    private void initializePlayer(){
        if(player == null){
            player = MediaPlayer.create(context, tuneID);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stop();
                }
            });
        }
    }

    public void playTune(){
       initializePlayer();
       player.start();
    }

    public void pauseTune(){
        if(player != null){
            player.pause();
        }
    }

    public void stopTune(){
        stop();
    }

    private void stop() {
      if(player != null){
          player.release();
          player = null;
      }
    }
}
