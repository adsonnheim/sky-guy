package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

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
            setContentView(R.layout.settings);
            checkWifiStatus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restart(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkWifiStatus() {
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
