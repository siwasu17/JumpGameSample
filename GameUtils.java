package com.example.yasu.jumpgamesample;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by yasu on 15/11/13.
 */
public class GameUtils {

    /**
     * 左上の色を透過色にしたBitmapを返す
     */
    public static Bitmap makeTransparent(Bitmap tmp) {
        int width = tmp.getWidth();
        int height = tmp.getHeight();
        int[] pixels = new int[width * height];
        int c = tmp.getPixel(0, 0);
        // 0,0 のピクセルと同じ色のピクセルを透明化する．
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixels[x + y * width] == c) {
                    pixels[x + y * width] = 0;
                }
            }
        }
        bitmap.eraseColor(Color.argb(0, 0, 0, 0));
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }
}
