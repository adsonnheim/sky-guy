package com.example.savethebunnyjava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOver extends AppCompatActivity {

    TextView tvPoints;
    TextView tvHighest;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;
    private SoundPool soundPool;
    private int clickSound;
    private boolean readyToPlay = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);
        tvPoints = findViewById(R.id.tvPoints);
        tvHighest = findViewById(R.id.tvHighest);
        ivNewHighest = findViewById(R.id.ivNewHighest);
        int points = getIntent().getExtras().getInt("points");
        tvPoints.setText("" + points);
        sharedPreferences = getSharedPreferences("my_pref", 0);
        //is sharedPreference a way to keep variables after app is closed?
        int highest = sharedPreferences.getInt("highest", 0);
        if (points > highest) {
            ivNewHighest.setVisibility(TextView.VISIBLE);
            highest = points;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highest", highest); //creation of the highest variable in sharedPreferences
            editor.commit();

        }
        tvHighest.setText("" + highest);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        clickSound = soundPool.load(this, R.raw.click, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (sampleId == clickSound) {
                readyToPlay = true;
            }
        });
    }
    public void restart(View view) {
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view) {
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        finish();
    }
}
