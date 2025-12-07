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
import android.media.AudioManager;
import android.media.SoundPool;
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
    final long UPDATE_MILLIS = 16;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint textPaintStage = new Paint();
    Paint healthPaint = new Paint();
    Paint paint = new Paint();
    private int resultColor = Color.WHITE;
    float TEXT_SIZE = 120;
    static int global = 10;
    int points = 0;
    int life = 3;
    int stage = 0;
    int stagePopupTime = 100;
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

    public SoundPool soundPool;
    public int dmgSound, lvlUpSound, birdDeathSound;
    public boolean readyToPlay = false;
    //instantiates objects to be used in the game view
    public GameView(Context context) {
        super(context);
        this.context = context;
        soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        dmgSound = soundPool.load(context, R.raw.dmgplayer, 1);
        lvlUpSound = soundPool.load(context, R.raw.lvlup, 1);
        birdDeathSound = soundPool.load(context, R.raw.bird, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0 && (sampleId == dmgSound || sampleId == lvlUpSound)) {
                readyToPlay = true; // now safe to play any loaded sound
            }
        });
        //playerSprite = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        //playerSprite = BitmapFactory.decodeResource(getResources(), R.drawable.hatguy);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        //display size
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        background = Bitmap.createScaledBitmap(
                background,
                dWidth,
                dHeight,
                false
        );
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
        //resetLeaderboard();
        if (readyToPlay) {
            soundPool.play(lvlUpSound, 1f, 1f, 3, 0, 1f);
        }
    }

    public void update() {
        // -------------------------
        // DAY/NIGHT CYCLE LOGIC -> sets resultColor
        // -------------------------
        int day = Color.rgb(143, 255, 255);
        int night = Color.rgb(10, 20, 60);
        int dawn = Color.rgb(255, 100, 80);
        int cycle = 6000;
        int phase = cycle / 3;   // = 2000
        int p = points % cycle;

        if (p < phase) {
            float t = p / (float) phase;
            resultColor = lerpColor(day, night, t);
        } else if (p < phase * 2) {
            float t = (p - phase) / (float) phase;
            resultColor = lerpColor(night, dawn, t);
        } else {
            float t = (p - phase * 2) / (float) phase;
            resultColor = lerpColor(dawn, day, t);
        }

        // -------------------------
        // CLOUD SPAWNING + MOVEMENT
        // -------------------------
        if (random.nextInt(100) == 0) { //1/100 each frame to spawn a cloud
            if (clouds.size() < 10) { //limits the amount of clouds on screen
                Cloud cloud = new Cloud(context);
                clouds.add(cloud);
            }
        }
        for (int i = clouds.size() - 1; i >= 0; i--) {
            Cloud c = clouds.get(i);
            c.cloudY += c.cloudGravity;
            if (c.cloudY > dHeight + 200) {
                clouds.remove(i);
            }
        }

        // -------------------------
        // STAGE POPUP
        // -------------------------
        if (points >= (stage * 1000)) {
            stage++;
            stagePopupTime = 100;
            if (readyToPlay) {
                soundPool.play(lvlUpSound, 1f, 1f, 3, 0, 1f);
            }
            Log.d("something", "STAGE");
        }
        if (stagePopupTime > 0) {
            stagePopupTime--;
        }

        // -------------------------
        // PLAYER MOVEMENT & MOMENTUM (updates player position & velocity)
        // -------------------------
        player.playerAnimation(); // keeps animation frame logic in sync

        if (isTouching && player.isPlayerGrabbed) {
            float dx = targetX - player.playerX;
            float dy = targetY - player.playerY;
            player.playerX += dx * player.playerFollowSpeed;
            player.playerY += dy * player.playerFollowSpeed;

            if (Math.abs(dx) < 2f && Math.abs(dy) < 2f) {
                player.playerX = targetX;
                player.playerY = targetY;
            }

            player.vx = dx * player.playerFollowSpeed * 2;
            player.vy = dy * player.playerFollowSpeed * 2;
        } else if (player.isMovingWithMomentum) {
            player.playerX += player.vx;
            player.playerY += player.vy;
            player.vx *= 0.75f;
            player.vy *= 0.75f;
            if (Math.abs(player.vx) < 0.1f && Math.abs(player.vy) < 0.1f) {
                player.isMovingWithMomentum = false;
            }
        }

        // -------------------------
        // BIRD LOGIC (movement + wall hit)
        // -------------------------
        Bird.birdRandomVelocity = 10 + ((int)Math.floor(points / 200) * 3);
        for (int i = 0; i < birds.size(); i++) {
            Bird b = birds.get(i);

            // animation frame
            b.birdFrame++;
            if (b.birdFrame > 2) b.birdFrame = 0;

            // movement
            b.birdX += b.birdVelocity;
            b.birdY += b.birdGravity;

            // bird touches side / bottom: spawn explosion, reset, sound, points
            if ((b.birdX + b.getBirdWidth() >= dWidth && b.facingRight) ||
                    (b.birdX < 0 && !b.facingRight) ||
                    (b.birdY + b.getBirdHeight() >= dHeight + 100)) {

                Explosion explosion = new Explosion(context);
                explosion.explosionX = b.birdX;
                explosion.explosionY = b.birdY;
                explosions.add(explosion);

                b.resetPosition();

                if (readyToPlay) {
                    soundPool.play(birdDeathSound, 0.5f, 0.5f, 1, 0, 1f);
                }
                points += 10;
            }
        }

        // -------------------------
        // BIRD <-> PLAYER COLLISIONS
        // -------------------------
        for (int i = 0; i < birds.size(); i++) {
            Bird b = birds.get(i);

            if (b.birdX + b.getBirdWidth() >= player.playerX - player.getPlayerWidth()/2f
                    && b.birdX <= player.playerX + player.getPlayerWidth() / 2f
                    && b.birdY + b.getBirdHeight() >= player.playerY - player.getPlayerHeight()/2f
                    && b.birdY <= player.playerY + player.getPlayerHeight()/2f) {

                life--;
                if (readyToPlay) {
                    soundPool.play(dmgSound, 1f, 1f, 1, 0, 1f);
                }

                Explosion explosion = new Explosion(context);
                explosion.explosionX = b.birdX;
                explosion.explosionY = b.birdY;
                explosions.add(explosion);

                b.resetPosition();

                // When the player dies (keep inline exactly as you had it)
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

                    // return early: game ended
                    return;
                }
            }
        }

        // -------------------------
        // EXPLOSIONS (advance frames and remove)
        // -------------------------
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            e.explosionFrame++;
            if (e.explosionFrame > 3) {
                explosions.remove(i);
            }
        }

        // Note: health color, points, etc. remain handled in draw or here as needed
    }

    //acts as a update function, running every frame
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // call update first (mutates game state)
        update();

        // create paint for background tinting (kept local as in your original)
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(resultColor, PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(background, 0, 0, paint);

        // draw clouds
        for (int i = 0; i < clouds.size(); i++) {
            Cloud c = clouds.get(i);
            canvas.drawBitmap(c.cloud[c.cloudFrame], c.cloudX, c.cloudY, null);
        }

        // stage popup text (fade handled by stagePopupTime updated in update())
        if (stagePopupTime > 0) {
            float t = stagePopupTime / 100f;
            int alpha = (int)(255 * t);
            textPaintStage.setAlpha(alpha);
            canvas.drawText("Stage " + stage, 300, TEXT_SIZE + 300, textPaintStage);
            textPaintStage.setAlpha(255);
        }

        // draw player
        canvas.drawBitmap(player.getPlayerSprite(player.playerCurrentFrame),
                player.playerX - player.getPlayerWidth()/ 2f,
                player.playerY - player.getPlayerHeight()/ 2f,
                null);

        // draw birds
        for (int i = 0; i < birds.size(); i++) {
            Bird b = birds.get(i);
            canvas.drawBitmap(b.getBird(b.birdFrame), b.birdX, b.birdY, null);
        }

        // draw explosions
        for (int i = 0; i < explosions.size(); i++) {
            Explosion e = explosions.get(i);
            canvas.drawBitmap(e.getExplosion(e.explosionFrame), e.explosionX, e.explosionY, null);
        }

        // health bar color
        if (life == 2) {
            healthPaint.setColor(Color.YELLOW);
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        } else {
            healthPaint.setColor(Color.GREEN);
        }

        int barBottom = 300;
        int maxHeight = 80;
        int currentHeight = (int)(maxHeight * life);

        // draw health bar
        canvas.drawRect(
                dWidth - 60,
                barBottom - currentHeight,
                dWidth - 20,
                barBottom,
                healthPaint
        );

        // stroke border (kept as you had it)
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(8);
        canvas.drawRect(dWidth - 60, barBottom - currentHeight, dWidth - 20, barBottom, borderPaint);

        // draw score
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);

        // schedule next frame
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

        //Log.d("myTag", "Leaderboard reset!");
    }

    public void updateLeaderboard(int points) {
        if (points == 0) {
            return;
        }

        SharedPreferences sp = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String currentUserAvatarUri = sp.getString("avatarUri", null);

        int newPosition = -1;
        for (int i = 0; i < 10; i++) {
            if (points > sp.getInt(String.valueOf(i) + "score", 0)) {
                newPosition = i;
                break;
            }
        }

        if (newPosition != -1) {
            for (int i = 8; i >= newPosition; i--) {
                int scoreToMove = sp.getInt(String.valueOf(i) + "score", 0);
                String avatarToMove = sp.getString("score" + (i + 1) + "Avatar", null);

                editor.putInt(String.valueOf(i + 1) + "score", scoreToMove);
                if (avatarToMove != null) {
                    editor.putString("score" + (i + 2) + "Avatar", avatarToMove);
                } else {
                    editor.remove("score" + (i + 2) + "Avatar");
                }
            }

            editor.putInt(String.valueOf(newPosition) + "score", points);
            if (currentUserAvatarUri != null) {
                editor.putString("score" + (newPosition + 1) + "Avatar", currentUserAvatarUri);
            } else {
                editor.remove("score" + (newPosition + 1) + "Avatar");
            }
        }

        editor.apply();
    }

    private int lerpColor(int c1, int c2, float t) {
        int r = (int) (Color.red(c1)   + (Color.red(c2)   - Color.red(c1)) * t);
        int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t);
        int b = (int) (Color.blue(c1)  + (Color.blue(c2)  - Color.blue(c1)) * t);
        return Color.rgb(r, g, b);
    }

}
