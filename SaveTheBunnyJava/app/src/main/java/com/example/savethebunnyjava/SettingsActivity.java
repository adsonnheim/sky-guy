package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class SettingsActivity extends BaseActivity {

    public static boolean isMusicPlaying = true;

    private ImageView avatarImageView;
    private Button selectAvatarButton;

    private ImageButton muteButton;
    private VideoView settingsVideoView;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String AVATAR_URI_KEY = "avatarUri";
    private int videoPlaybackPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        muteButton = findViewById(R.id.muteButton);

        avatarImageView = findViewById(R.id.avatarImageView);
        selectAvatarButton = findViewById(R.id.selectAvatarButton);
        settingsVideoView = findViewById(R.id.settingsVideoView);

        setupVideoPlayer();

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // These 2 lines allow for persistent permission to access the image
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

                        avatarImageView.setImageURI(selectedImageUri);
                        saveAvatarUri(selectedImageUri);
                    }
                }
            });

        selectAvatarButton.setOnClickListener(v -> {
            if (readyToPlay) {
                soundPool.play(clickSound, 1f, 1f, 1,  0, 1f);
            }

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        clickSound = soundPool.load(this, R.raw.click, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (sampleId == clickSound) {
                readyToPlay = true;
            }
        });

        loadAvatar();
    }

    private void setupVideoPlayer() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nice;
        Uri uri = Uri.parse(videoPath);
        settingsVideoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        settingsVideoView.setMediaController(mediaController);
        mediaController.setAnchorView(settingsVideoView);

        settingsVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            settingsVideoView.post(settingsVideoView::pause);
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (settingsVideoView.isPlaying()) {
            videoPlaybackPosition = settingsVideoView.getCurrentPosition();
            settingsVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!settingsVideoView.isPlaying()) {
            settingsVideoView.seekTo(videoPlaybackPosition);
            settingsVideoView.start();
        }
    }

    private void saveAvatarUri(Uri uri) {
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(AVATAR_URI_KEY, uri.toString());
        editor.apply();
    }

    private void loadAvatar() {
        if (readyToPlay) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = sp.getString(AVATAR_URI_KEY, null);

        if (uriString != null) {
            Uri savedUri = Uri.parse(uriString);
            avatarImageView.setImageURI(savedUri);
        }
    }

    public void mute(View view) {
        if (isMusicPlaying) {
            stopService(new Intent(this, MusicService.class));
            muteButton.setImageResource(R.drawable.musiciconoff);
            isMusicPlaying = false;
        } else {
            startService(new Intent(this, MusicService.class));
            muteButton.setImageResource(R.drawable.musiconon);
            isMusicPlaying = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}