package com.example.savethebunnyjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Player {
    enum animationStates{IDLE, HURT};
    Bitmap[] playerSprites = new Bitmap[2];
    animationStates playerState = animationStates.IDLE;
    float playerX, playerY;
    float playerFollowSpeed = 0.2f;
    float vx = 0, vy = 0;
    boolean isMovingWithMomentum = false;
    boolean isPlayerGrabbed = false;
    int playerAnimationDelay = 10, playerCurrentFrame = 0; //how many frames until the player frame swicthes, and which frame is currently drawn
    public Player(Context context) {
        playerSprites[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hatguy1);
        playerSprites[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hatguy2);
    }
    public void setPlayerPosition(float playerX, float playerY) {
        this.playerX = playerX;
        this.playerY = playerY;
    }
    public void playerAnimation() {
        if (playerState.equals(animationStates.IDLE)) {
            playerAnimationDelay--;
            if (playerAnimationDelay <= 0) {
                if (playerCurrentFrame >= 1) {
                    playerCurrentFrame = 0;
                } else {
                    playerCurrentFrame++;
                }
                playerAnimationDelay = 10;
            }
        }
    }
    public Bitmap getPlayerSprite(int spriteFrame) {return playerSprites[spriteFrame];}
    public int getPlayerWidth() {
        return playerSprites[0].getWidth();
    }

    public int getPlayerHeight() {
        return playerSprites[0].getHeight();
    }
}
