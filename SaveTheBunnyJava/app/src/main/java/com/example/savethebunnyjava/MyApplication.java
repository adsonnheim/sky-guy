package com.example.savethebunnyjava;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.net.CookieHandler;

public class MyApplication extends Application implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();
        //muted music since VM seems to have trouble playing it
        //ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        // App in background
        stopService(new Intent(this, MusicService.class));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        // App in foreground
        if (!SettingsActivity.isMusicPlaying) {
            return;
        } else {
            startService(new Intent(this, MusicService.class));
        }
    }
}
