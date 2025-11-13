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
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View{
    Bitmap background, ground, playerSprite;//defining the starting objects
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    Player player;
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
    //float playerX, playerY;
    //float oldX;
    //float oldY;
    //float oldRabbitX;
    //float oldRabbitY;
    //float playerFollowSpeed = 0.2f;
    //float snapThreshold = 15f;
    //float vx = 0, vy = 0;       // playerSprite velocity
    float targetX, targetY;
    boolean isTouching = false; //holds true if user is touching screen
   // boolean isMovingWithMomentum = false;
   // boolean isPlayerGrabbed = false;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;
    //instantiates objects to be used in the game view
    public GameView(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        playerSprite = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
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
        float startX = dWidth / 2f - playerSprite.getWidth() / 2f;
        float startY = dHeight - ground.getHeight() - playerSprite.getHeight();
        player = new Player(startX, startY);
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
        canvas.drawBitmap(playerSprite, player.playerX - playerSprite.getWidth()/ 2f, player.playerY - playerSprite.getHeight()/ 2f, null);
        if (isTouching && player.isPlayerGrabbed) {
            // Smoothly move toward touch target
            float dx = targetX - player.playerX;
            float dy = targetY - player.playerY;
            player.playerX += dx * player.playerFollowSpeed;
            player.playerY += dy * player.playerFollowSpeed;

            if (Math.abs(dx) < 2f && Math.abs(dy) < 2f) {
                player.playerX = targetX;
                player.playerY = targetY;
            }

            // Update velocity for momentum
            player.vx = dx * player.playerFollowSpeed * 2;
            player.vy = dy * player.playerFollowSpeed * 2;
        } else if (player.isMovingWithMomentum) {
            // Momentum after finger lift
            player.playerX += player.vx;
            player.playerY += player.vy;
            player.vx *= 0.75f;
            player.vy *= 0.75f;

            if (Math.abs(player.vx) < 0.1f && Math.abs(player.vy) < 0.1f) {
                player.isMovingWithMomentum = false;
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
            if (spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= player.playerX - playerSprite.getWidth()/2f
            && spikes.get(i).spikeX <= player.playerX + playerSprite.getWidth() / 2f
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= player.playerY - playerSprite.getHeight()/2f
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= player.playerY + playerSprite.getHeight()/2f) {
                //collision happened
                //Note: made playerSprite immortal for testing purposes
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
                if (touchX >= (player.playerX - playerSprite.getWidth() / 2) - 50 &&
                        touchX <= (player.playerX + playerSprite.getWidth() / 2) + 50 &&
                        touchY >= (player.playerY - playerSprite.getHeight() / 2) - 50 &&
                        touchY <= (player.playerY + playerSprite.getHeight() / 2) + 50) {
                    player.isPlayerGrabbed = true;
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
                player.isPlayerGrabbed = false;
                player.isMovingWithMomentum = true;
                break;
        }

        return true;
    }
}
