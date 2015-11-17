package com.example.yasu.jumpgamesample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yasu on 15/10/27.
 */
public class GameView extends View implements Angel.Callback{
    private Angel angel;
    private static final int START_GROUND_HEIGHT = 50;
    private static final int GROUND_MOVE_TO_LEFT = 10;
    private Ground ground;

    private static final int MAX_TOUCH_TIME = 500; //msec

    private long touchDownStartTime;

    public GameView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchDownStartTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                jumpAngel();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void jumpAngel(){
        float time = System.currentTimeMillis() - touchDownStartTime;
        touchDownStartTime = 0;

        //ジャンプできるのは地面にいるときだけ
        if(getDistanceFromGround(angel) != 0){
            return;
        }

        if(time > MAX_TOUCH_TIME){
            time = MAX_TOUCH_TIME;
        }

        angel.jump(time/MAX_TOUCH_TIME);


    }

    public void onDraw(Canvas canvas){
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if(angel == null){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.angel);
            angel = new Angel(bitmap,0,0,this);
        }

        if(ground == null){
            ground = new Ground(0,height - START_GROUND_HEIGHT,width,height);
        }

        angel.move();
        angel.draw(canvas);

        ground.move(GROUND_MOVE_TO_LEFT);
        ground.draw(canvas);

        invalidate();
    }

    /**
     * 地面との距離を返す関数の実装
     * @param angel
     * @return 地面からの距離
     */
    @Override
    public int getDistanceFromGround(Angel angel) {
        boolean horizontal
                = !(angel.rect.left >= ground.rect.right || angel.rect.right <= ground.rect.left);
        if(!horizontal){
            return Integer.MAX_VALUE;
        }
        return ground.rect.top - angel.rect.bottom;
    }
}
