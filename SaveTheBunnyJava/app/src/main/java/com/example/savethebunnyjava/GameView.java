package com.example.savethebunnyjava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class GameView extends View{
    Bitmap background, ground, rabbit;
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;

    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 120;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    //Rabbit(AKA PLAYER)
    float rabbitX, rabbitY;
    float oldX;
    float oldY;
    float oldRabbitX;
    float oldRabbitY;
    float rabbitFollowSpeed = 0.2f;
    float snapThreshold = 15f;
    float vx = 0, vy = 0;       // rabbit velocity
    float targetX, targetY;
    boolean isTouching = false; //holds true if user is touching screen
    boolean isMovingWithMomentum = false;
    boolean isRabbitGrabbed = false;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;
    //instantiates objects to be used in the game view
    public GameView(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        rabbit = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0,0,dWidth,dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        textPaint.setColor(Color.rgb(255, 165, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        healthPaint.setColor(Color.GREEN);
        random = new Random();
        rabbitX = dWidth / 2 - rabbit.getWidth() / 2;
        rabbitY = dHeight - ground.getHeight() - rabbit.getHeight();
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++) { //creates spikes
            Spike spike = new Spike(context);
            spikes.add(spike);
        }

    }
    //acts as a update function, running every frame
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(rabbit, rabbitX - rabbit.getWidth()/ 2, rabbitY - rabbit.getHeight()/ 2, null);
        if (isTouching && isRabbitGrabbed) {
            // Smoothly move toward touch target
            float dx = targetX - rabbitX;
            float dy = targetY - rabbitY;
            rabbitX += dx * rabbitFollowSpeed;
            rabbitY += dy * rabbitFollowSpeed;

            // Optional snap threshold
            if (Math.abs(dx) < 2f && Math.abs(dy) < 2f) {
                rabbitX = targetX;
                rabbitY = targetY;
            }

            // Update velocity for potential momentum after release
            vx = dx * rabbitFollowSpeed * 2;
            vy = dy * rabbitFollowSpeed * 2;
        } else if (isMovingWithMomentum) {
            // Momentum after finger lift
            rabbitX += vx;
            rabbitY += vy;
            vx *= 0.75f;
            vy *= 0.75f;

            if (Math.abs(vx) < 0.1f && Math.abs(vy) < 0.1f) {
                isMovingWithMomentum = false;
            }
        }
        for (int i = 0; i < spikes.size(); i++) {//draws spikes
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            spikes.get(i).spikeFrame++;
            if (spikes.get(i).spikeFrame > 2) { //controls spikes animation
                spikes.get(i).spikeFrame = 0;
            }
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spikes.get(i).spikeX;
                explosion.explosionY = spikes.get(i).spikeY;
                explosions.add(explosion);
                spikes.get(i).resetPosition();

            }
        }

        for (int i = 0; i < spikes.size(); i++) {
            if (spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= rabbitX - rabbit.getWidth()/2
            && spikes.get(i).spikeX <= rabbitX + rabbit.getWidth() / 2
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= rabbitY - rabbit.getHeight()/2
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= rabbitY + rabbit.getHeight()/2) {
                //collision happened
                //Note: made rabbit immortal for testing purposes
                //life--;
                spikes.get(i).resetPosition();
                if (life == 0) {
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }

        for (int i = 0; i < explosions.size(); i++) {
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX,
                    explosions.get(i).explosionY, null);
            explosions.get(i).explosionFrame++;
            if (explosions.get(i).explosionFrame > 3) {
                explosions.remove(i);
            }
        }

        if (life == 2) {
            healthPaint.setColor(Color.YELLOW);
        } else if (life == 1) {
            healthPaint.setColor((Color.RED));
        }
        canvas.drawRect(dWidth-200, 30, dWidth-200+60*life, 80, healthPaint);

        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }
    //seems like the top left corner of the screen is coords (0, 0)
    //sprites coords are at the top left aswell
    //Movement
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (touchX >= (rabbitX - rabbit.getWidth() / 2) - 50 && touchX <= (rabbitX + rabbit.getWidth() / 2) + 50 && //the +/-50 are there so player doesn't have to exactly touch rabbit
                        touchY >= (rabbitY - rabbit.getHeight()/2) - 50 && touchY <= (rabbitY + rabbit.getHeight()/2) + 50) {
                    isRabbitGrabbed = true;
                }
                isTouching = true;
                targetX = touchX;
                targetY = touchY;
                break;

            case MotionEvent.ACTION_MOVE:
                targetX = touchX;
                targetY = touchY;
                break;

            case MotionEvent.ACTION_UP:
                isTouching = false;
                isRabbitGrabbed = false;
                isMovingWithMomentum = true; // optional momentum after release
                break;
        }

        return true;
    }
}
