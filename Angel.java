package com.example.yasu.jumpgamesample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by yasu on 15/11/13.
 */
public class Angel {

    private final Paint paint = new Paint();
    private Bitmap bitmap;
    final Rect rect;

    private static final float GRAVITY = 0.8f;
    private static final float WEIGHT = GRAVITY * 60;
    private float acceleration = 0;

    public interface Callback{
        public int getDistanceFromGround(Angel angel);
    }

    private final Callback callback;


    public Angel(Bitmap bitmap, int left, int top, Callback callback) {
        this.rect = new Rect(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
        this.bitmap = GameUtils.makeTransparent(bitmap);
        this.callback = callback;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
    }

    public void jump(float power){
        acceleration = power * WEIGHT;
    }

    public void move() {
        acceleration -= GRAVITY;
        int distanceFromGround = callback.getDistanceFromGround(this);
        if(acceleration < 0 && acceleration < -distanceFromGround){
            acceleration = -distanceFromGround;
        }

        rect.offset(0, -Math.round(acceleration));
    }
}
