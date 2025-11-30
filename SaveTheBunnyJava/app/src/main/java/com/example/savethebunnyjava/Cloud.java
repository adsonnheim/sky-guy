package com.example.savethebunnyjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Random;
import java.util.logging.Logger;

public class Cloud {
    Bitmap [] cloud = new Bitmap[3];
    int cloudFrame = 0;
    int cloudX, cloudY;
    float cloudGravity = 2;
    Random random;
    public Cloud(Context context) {
        cloud[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1);
        cloud[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2);
        cloud[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud3);
        random = new Random();
        cloudFrame = random.nextInt(3);
        resetPosition();
    }
    public int getCloudWidth() {
        return cloud[0].getWidth();
    }

    public int getCloudHeight() {
        return cloud[0].getHeight();
    }
    public void resetPosition() {
            cloudX = random.nextInt(GameView.dWidth);
            cloudY = -200;
    }
}
