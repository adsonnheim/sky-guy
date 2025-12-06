package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LeaderboardActivity extends BaseActivity {

    TextView test, score1, score2, score3, score4, score5, score6, score7, score8, score9, score10;
    ImageView score1Avatar, score2Avatar, score3Avatar, score4Avatar, score5Avatar, score6Avatar, score7Avatar, score8Avatar, score9Avatar, score10Avatar;

    ImageView[] avatarViews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        //test = findViewById(R.id.top_points_text);
        score1 = findViewById(R.id.score1);
        score2 = findViewById(R.id.score2);
        score3 = findViewById(R.id.score3);
        score4 = findViewById(R.id.score4);
        score5 = findViewById(R.id.score5);
        score6 = findViewById(R.id.score6);
        score7 = findViewById(R.id.score7);
        score8 = findViewById(R.id.score8);
        score9 = findViewById(R.id.score9);
        score10 = findViewById(R.id.score10);

        score1Avatar = findViewById(R.id.score1Avatar);
        score2Avatar = findViewById(R.id.score2Avatar);
        score3Avatar = findViewById(R.id.score3Avatar);
        score4Avatar = findViewById(R.id.score4Avatar);
        score5Avatar = findViewById(R.id.score5Avatar);
        score6Avatar = findViewById(R.id.score6Avatar);
        score7Avatar = findViewById(R.id.score7Avatar);
        score8Avatar = findViewById(R.id.score8Avatar);
        score9Avatar = findViewById(R.id.score9Avatar);
        score10Avatar = findViewById(R.id.score10Avatar);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        clickSound = soundPool.load(this, R.raw.click, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (sampleId == clickSound) {
                readyToPlay = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScore();
    }

    private void updateScore() {
        SharedPreferences sp = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);

        //int highScore = sp.getInt("highScore", 0);

        //test.setText("Top Score: " + String.valueOf(highScore));

        TextView[] scoreViews = {score1, score2, score3, score4, score5, score6, score7, score8, score9, score10};

        for (int i = 0; i < 10; i++) {
            scoreViews[i].setText((i + 1) + ". " + String.valueOf(sp.getInt(i + "score", 0)));
        }

        ImageView[] avatarViews = {score1Avatar, score2Avatar, score3Avatar, score4Avatar, score5Avatar, score6Avatar, score7Avatar, score8Avatar, score9Avatar, score10Avatar};
        String[] avatarKeys = {"score1Avatar", "score2Avatar", "score3Avatar", "score4Avatar", "score5Avatar", "score6Avatar", "score7Avatar", "score8Avatar", "score9Avatar", "score10Avatar"};

        for (int i = 0; i < 10; i++) {
            String uriString = sp.getString(avatarKeys[i], null);

            if (uriString != null) {
                avatarViews[i].setImageURI(Uri.parse(uriString));
            } else {
                avatarViews[i].setImageResource(R.drawable.rabbit);
            }
        }

    }

    public void resetLeaderboard(View view) {

        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        SharedPreferences sp = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        for (int i = 0; i < 10; i++) {
            editor.putInt(String.valueOf(i) + "score", 0);
        }

        for (int i = 1; i < 11; i++) {
            editor.putString("score" + i + "Avatar", null);
        }

        editor.apply();

        Log.d("myTag", "Leaderboard reset!");
        updateScore();
    }
}