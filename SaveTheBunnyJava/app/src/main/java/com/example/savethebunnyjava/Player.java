package com.example.savethebunnyjava;

public class Player {
    float playerX, playerY;
    float playerFollowSpeed = 0.2f;
    float vx = 0, vy = 0;
    boolean isMovingWithMomentum = false;
    boolean isPlayerGrabbed = false;
    public Player(float playerX, float playerY) {
        this.playerX = playerX;
        this.playerY = playerY;
    }
}
