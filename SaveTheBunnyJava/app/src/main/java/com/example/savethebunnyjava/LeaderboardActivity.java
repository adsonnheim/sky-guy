package com.example.savethebunnyjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class LeaderboardActivity extends BaseActivity {

    TextView test, score1, score2, score3, score4, score5, score6, score7, score8, score9, score10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        test = findViewById(R.id.top_points_text);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScore();
    }

    private void updateScore() {
        SharedPreferences sp = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);

        int highScore = sp.getInt("highScore", 0);

        test.setText(String.valueOf(highScore));

        score1.setText("1. " + String.valueOf(sp.getInt("0score", 0)));
        score2.setText("2. " + String.valueOf(sp.getInt("1score", 0)));
        score3.setText("3. " + String.valueOf(sp.getInt("2score", 0)));
        score4.setText("4. " + String.valueOf(sp.getInt("3score", 0)));
        score5.setText("5. " + String.valueOf(sp.getInt("4score", 0)));
        score6.setText("6. " + String.valueOf(sp.getInt("5score", 0)));
        score7.setText("7. " + String.valueOf(sp.getInt("6score", 0)));
        score8.setText("8. " + String.valueOf(sp.getInt("7score", 0)));
        score9.setText("9. " + String.valueOf(sp.getInt("8score", 0)));
        score10.setText("10. " + String.valueOf(sp.getInt("9score", 0)));
    }
}