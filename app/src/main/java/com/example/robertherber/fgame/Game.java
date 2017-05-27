package com.example.robertherber.fgame;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Game extends AppCompatActivity {

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new GamePanel(this));
        sound(0);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mp.stop();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mp.stop();
    }

    public void sound(int b){
        if(b == 0){
            mp = MediaPlayer.create(this, R.raw.america);
        }
        mp.setVolume((float)0.2, (float) 0.2);
        mp.setLooping(true);
        mp.start();
    }
}
