package com.example.savethebunnyjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.Random;

public class Bird {
    Bitmap[] bird = new Bitmap[3];
    Bitmap[] birdFlipped = new Bitmap[3];
    boolean facingRight = false;
    int birdFrame = 0;
    int birdX, birdY, birdVelocity;
    static int birdRandomVelocity = 5;
    float birdGravity = 2;
    Random random;

    public Bird(Context context) {
        bird[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bird1);
        bird[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bird2);
        bird[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bird3);
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1); // horizontal flip

        for (int i = 0; i < 3; i++) {
            birdFlipped[i] = Bitmap.createBitmap(
                    bird[i],
                    0, 0,
                    bird[i].getWidth(),
                    bird[i].getHeight(),
                    matrix,
                    true
            );
        }
        random = new Random();
        resetPosition();
    }

    public Bitmap getBird(int birdFrame) {
        return facingRight ? bird[birdFrame] : birdFlipped[birdFrame];
    }

    public int getBirdWidth() {
        return bird[0].getWidth();
    }

    public int getBirdHeight() {
        return bird[0].getHeight();
    }

    public void resetPosition() {
        if (random.nextInt(2) == 0) {
            birdX = -200 + random.nextInt(600) * -1;
            birdY = random.nextInt(GameView.dHeight - getBirdHeight());
            birdVelocity = 5 + random.nextInt(birdRandomVelocity);
        } else {
            birdX = 1000 + random.nextInt(600);
            birdY = random.nextInt(GameView.dHeight - getBirdHeight());
            birdVelocity = (5 + random.nextInt(birdRandomVelocity)) * -1;
        }
    }
}