package com.example.savethebunnyjava;

import android.content.Context;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //Test by Jesus
        // Test comment by Alan
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void startGame(View view) {
        // TODO
        GameView gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            setContentView(R.layout.settings);
            checkWifiStatus();
            return true;
        }
        if (item.getItemId() == R.id.home) {
            setContentView(R.layout.activity_main);
            return true;
        }
        if (item.getItemId() == R.id.leaderboard) {
            setContentView(R.layout.leaderboard);
            return true;
        }
        return super.onOptionsItemSelected(item);
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