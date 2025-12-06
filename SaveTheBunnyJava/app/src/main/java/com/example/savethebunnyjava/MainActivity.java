package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.savethebunnyjava.R;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        clickSound = soundPool.load(this, R.raw.click, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (sampleId == clickSound) {
                readyToPlay = true;
            }
        });
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void startGame(View view) {
        // TODO
        //musicManager.playSound(1, 1f);
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        GameView gameView = new GameView(this);
        setContentView(gameView);
    }

}