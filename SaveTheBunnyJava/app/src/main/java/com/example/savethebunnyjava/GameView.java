package com.example.savethebunnyjava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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

public class GameView extends View {
    Bitmap background, ground;//defining the starting objects
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    Player player;
    final long UPDATE_MILLIS = 20;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint textPaintStage = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 120;
    static int global = 10;
    int points = 0;
    int life = 3;
    int stage = 0;
    int stagePopupTime = 70;
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
    ArrayList<Bird> birds;
    ArrayList<Explosion> explosions;
    ArrayList<Cloud> clouds;
    //instantiates objects to be used in the game view
    public GameView(Context context) {
        super(context);
        this.context = context;

        //playerSprite = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        //playerSprite = BitmapFactory.decodeResource(getResources(), R.drawable.hatguy);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        //display size
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        //start position of player
        player = new Player(context);
        float startX = dWidth / 2f - player.getPlayerWidth() / 2f;
        float startY = dHeight - 100 - player.getPlayerHeight();
        player.setPlayerPosition(startX, startY);
        rectBackground = new Rect(0,0,dWidth,dHeight);
        //rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
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
        textPaintStage = textPaint;
        healthPaint.setColor(Color.GREEN);
        random = new Random();

        birds = new ArrayList<>();
        explosions = new ArrayList<>();
        clouds = new ArrayList<>();
        for (int i = 0; i < 5; i++) { //creates birds
            Bird bird = new Bird(context);
            birds.add(bird);
            if (birds.get(i).birdVelocity > 0) birds.get(i).facingRight = true;
            else birds.get(i).facingRight = false;
        }
        for (int i = 0; i < 5; i++) { //creates birds
            Cloud cloud = new Cloud(context);
            clouds.add(cloud);
            clouds.get(i).cloudY = random.nextInt(dHeight);
        }

    }

    //acts as a update function, running every frame
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        int sky = Color.rgb(143, 255, 255);
        int night = Color.rgb(10, 20, 60);
        int dawn = Color.rgb(255, 100, 80);

        int resultColor;

        if (points <= 5000) {
            float t = points / 5000f;            // 0 → 1
            resultColor = lerpColor(sky, night, t);
        }
        else if (points <= 10000) {
            float t = (points - 10000) / 10000f;  // 0 → 1
            resultColor = lerpColor(night, dawn, t);
        }
        else {
            resultColor = dawn;
        }
        paint.setColorFilter(new PorterDuffColorFilter(resultColor, PorterDuff.Mode.SRC_ATOP));

        canvas.drawBitmap(background, null, rectBackground, paint);
        //triggers stage popup
        if (points >= (stage * 1000)) {
            stage++;
            stagePopupTime = 70;
        }
        if (stagePopupTime > 0) {
            stagePopupTime--;

            // fade: 0.0 → 1.0
            float t = stagePopupTime / 70f;

            // convert to 0–255 alpha
            int alpha = (int)(255 * t);
            textPaintStage.setAlpha(alpha);

            canvas.drawText("Stage " + stage, 300, TEXT_SIZE, textPaintStage);
            textPaintStage.setAlpha(255);
        }
        //canvas.drawText("Stage " + stage, 600, TEXT_SIZE, textPaint);

        if (random.nextInt(100) == 0) { //1/100 each frame to spawn a cloud
            if (clouds.size() < 10) { //limits the amount of clouds on screen
                Cloud cloud = new Cloud(context);
                clouds.add(cloud);
            }
        }
        for (int i = 0; i < clouds.size(); i++) {
            canvas.drawBitmap(clouds.get(i).cloud[clouds.get(i).cloudFrame], clouds.get(i).cloudX, clouds.get(i).cloudY, null);
            clouds.get(i).cloudY += clouds.get(i).cloudGravity;
            if (clouds.get(i).cloudY > dHeight + 200) {
                clouds.remove(i);
            }
        }

        //canvas.drawBitmap(ground, null, rectGround, null);

        player.playerAnimation();//plays the idle animation
        canvas.drawBitmap(player.getPlayerSprite(player.playerCurrentFrame), player.playerX - player.getPlayerWidth()/ 2f, player.playerY - player.getPlayerHeight()/ 2f, null);

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
        Bird.birdRandomVelocity = 5 + ((int)Math.floor(points / 500) * 3);
        for (int i = 0; i < birds.size(); i++) {//draws birds
            canvas.drawBitmap(birds.get(i).getBird(birds.get(i).birdFrame), birds.get(i).birdX, birds.get(i).birdY, null);
            birds.get(i).birdFrame++;
            if (birds.get(i).birdFrame > 2) { //controls birds animation
                birds.get(i).birdFrame = 0;
            }
            //bird movement
            birds.get(i).birdX += birds.get(i).birdVelocity;
            birds.get(i).birdY += birds.get(i).birdGravity;

            if ((birds.get(i).birdX + birds.get(i).getBirdWidth() >= dWidth && birds.get(i).facingRight) ||
                    (birds.get(i).birdX < 0 && !birds.get(i).facingRight) || (birds.get(i).birdY + birds.get(i).getBirdHeight() >= dHeight + 100)) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = birds.get(i).birdX;
                explosion.explosionY = birds.get(i).birdY;
                explosions.add(explosion);
                birds.get(i).resetPosition();

            }
        }

        for (int i = 0; i < birds.size(); i++) {
            if (birds.get(i).birdX + birds.get(i).getBirdWidth() >= player.playerX - player.getPlayerWidth()/2f
            && birds.get(i).birdX <= player.playerX + player.getPlayerWidth() / 2f
            && birds.get(i).birdY + birds.get(i).getBirdHeight() >= player.playerY - player.getPlayerHeight()/2f
            && birds.get(i).birdY <= player.playerY + player.getPlayerHeight()/2f) {
                //collision happened
                //Note: made playerSprite immortal for testing purposes
                //life--;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = birds.get(i).birdX;
                explosion.explosionY = birds.get(i).birdY;
                explosions.add(explosion);
                birds.get(i).resetPosition();

                // When the player dies
                if (life == 0) {

                    SharedPreferences sp = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();

                    int currentHighScore = sp.getInt("highScore", 0);
                    if (points > currentHighScore) {
                        editor.putInt("highScore", points);
                        editor.apply();
                    }

                    //resetLeaderboard();
                    updateLeaderboard(points);

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
                if (touchX >= (player.playerX - player.getPlayerWidth() / 2) - 50 &&
                        touchX <= (player.playerX + player.getPlayerWidth() / 2) + 50 &&
                        touchY >= (player.playerY - player.getPlayerHeight() / 2) - 50 &&
                        touchY <= (player.playerY + player.getPlayerHeight() / 2) + 50) {
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

    public void resetLeaderboard() {
        SharedPreferences sp = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        for (int i = 0; i < 10; i++) {
            editor.putInt(String.valueOf(i) + "score", 0);
            editor.apply();
        }

        Log.d("myTag", "Leaderboard reset!");
    }

    public void updateLeaderboard(int points) {
        SharedPreferences sp = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        int previousVal = 0;
        int lastUpdatedIndex = 0;
        int totalValues = 0;

        for (int i = 0; i < 10; i++) {
            int curScore = sp.getInt(String.valueOf(i) + "score", 0);

            if (points > curScore) {
                previousVal = curScore;
                editor.putInt(String.valueOf(i) + "score", points);
                editor.apply();
                lastUpdatedIndex = i;
                break;
            }
        }

        for (int i = 0; i < 10; i++) {
            if (sp.getInt(String.valueOf(i) + "score", 0) != 0) {
                totalValues++;
            }
        }

        for (int k = lastUpdatedIndex + 1; k < totalValues + 1; k++) {
            int currentScore = sp.getInt(String.valueOf(k) + "score", 0);

            if (k < totalValues + 1) {
                editor.putInt(String.valueOf(k) + "score", previousVal);
                editor.apply();
            }

            previousVal = currentScore;
        }

        Log.d("myTag", "Current Leaderboard : ");
        for (int i = 0; i < 10; i++) {
            Log.d("myTag", String.valueOf(sp.getInt(String.valueOf(i) + "score", 0)));
        }
    }

    private int lerpColor(int c1, int c2, float t) {
        int r = (int) (Color.red(c1)   + (Color.red(c2)   - Color.red(c1)) * t);
        int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t);
        int b = (int) (Color.blue(c1)  + (Color.blue(c2)  - Color.blue(c1)) * t);
        return Color.rgb(r, g, b);
    }

}
