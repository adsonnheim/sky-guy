package com.example.savethebunnyjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class SettingsActivity extends BaseActivity {

    private ImageView avatarImageView;
    private Button selectAvatarButton;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String AVATAR_URI_KEY = "avatarUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        avatarImageView = findViewById(R.id.avatarImageView);
        selectAvatarButton = findViewById(R.id.selectAvatarButton);

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
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        loadAvatar();
    }

    private void saveAvatarUri(Uri uri) {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(AVATAR_URI_KEY, uri.toString());
        editor.apply();
    }

    private void loadAvatar() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = sp.getString(AVATAR_URI_KEY, null);

        if (uriString != null) {
            Uri savedUri = Uri.parse(uriString);
            avatarImageView.setImageURI(savedUri);
        }
    }
}