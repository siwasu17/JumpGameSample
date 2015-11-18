package com.example.yasu.jumpgamesample;

import android.graphics.Canvas;

/**
 * Created by yasu on 15/11/19.
 */
public class Blank extends Ground {
    public Blank(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        //Draw Nothing
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
