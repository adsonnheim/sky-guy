package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    public SoundPool soundPool;
    public int clickSound;
    public boolean readyToPlay = false;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        if (itemId == R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.leaderboard) {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restart(View view) {
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void checkWifiStatus() {
        TextView wTextView = findViewById(R.id.wifi_status_text);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());

        String status;
        if (nc == null) {
            status = "Status: Disconnected";
        } else {
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                status = "Status: Connected to Wi-Fi";
            } else {
                status = "Status: Not Connected to Wi-Fi";
            }
        }

        wTextView.setText(status);
    }
}
