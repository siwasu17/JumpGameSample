package com.example.yasu.jumpgamesample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by yasu on 15/10/27.
 */
public class GameView extends SurfaceView implements Angel.Callback, SurfaceHolder.Callback {

    /**
     * Game Loop
     */
    private static final long FPS = 60;

    private class DrawThread extends Thread {
        boolean isFinished;

        @Override
        public void run() {
            SurfaceHolder holder = getHolder();
            while (!isFinished) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawGame(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
                try {
                    sleep(1000 / FPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DrawThread drawThread;

    public void startDrawThread() {
        stopDrawThread();

        drawThread = new DrawThread();
        drawThread.start();
    }

    public boolean stopDrawThread() {
        if (drawThread == null) {
            return false;
        }

        drawThread.isFinished = true;
        drawThread = null;
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDrawThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //NOTHING
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }



    /**
     * Game Main
     */
    private static final int START_GROUND_HEIGHT = 50;
    private static final int GROUND_MOVE_TO_LEFT = 10;
    private static final int ADD_GROUND_COUNT = 5;
    private static final int GROUND_WIDTH = 340;
    private static final int GROUND_BLOCK_HEIGHT = 100;
    private static final int POWER_GAUGE_HEIGHT = 30;
    private static final Paint PAINT_POWER_GAUGE = new Paint();
    static {
        PAINT_POWER_GAUGE.setColor(Color.RED);
    }

    public interface Callback{
        void onGameOver();
    }

    private Callback callback;

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    private final Handler handler;
    private boolean isGameOver;
    private boolean needRestart;

    private Angel angel;
    private Ground lastGround;
    private final List<Ground> groundList = new ArrayList<Ground>();
    private final Random rand = new Random();

    private static final int MAX_TOUCH_TIME = 500; //msec

    private long touchDownStartTime;

    public GameView(Context context) {
        super(context);

        handler = new Handler();

        getHolder().addCallback(this);
    }

    private void gameOver(){
        if(isGameOver){
            return;
        }
        isGameOver = true;
        angel.shutdown();

        //非同期にメインスレッドに通知するためHandlerを使う
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onGameOver();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownStartTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                jumpAngel();

                if(isGameOver){
                    restartGame();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void restartGame(){
        isGameOver = false;
        angel.reset();
    }

    private void jumpAngel() {
        float time = System.currentTimeMillis() - touchDownStartTime;
        touchDownStartTime = 0;

        //ジャンプできるのは地面にいるときだけ
        if (getDistanceFromGround(angel) != 0) {
            return;
        }

        if (time > MAX_TOUCH_TIME) {
            time = MAX_TOUCH_TIME;
        }

        angel.jump(time / MAX_TOUCH_TIME);
    }

    public void drawGame(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawColor(Color.WHITE);

        if (angel == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.angel);
            angel = new Angel(bitmap, 0, 0, this);

            //開始時の地面
            lastGround = new Ground(0, height - START_GROUND_HEIGHT, width, height);
            groundList.add(lastGround);
        }

        if (lastGround.isShown(width, height)) {
            for (int i = 0; i < ADD_GROUND_COUNT; i++) {
                int left = lastGround.rect.right;
                int groundHeight = rand.nextInt(height / GROUND_BLOCK_HEIGHT) * GROUND_BLOCK_HEIGHT / 2 + START_GROUND_HEIGHT;

                if(i % 2 == 0){
                    lastGround = new Ground(left, height - groundHeight,
                            left + GROUND_WIDTH, height);
                }else{
                    lastGround = new Blank(left, height - 1,
                            left + GROUND_WIDTH, height);
                }
                groundList.add(lastGround);
            }
        }

        for (int i = 0; i < groundList.size(); i++) {
            Ground g = groundList.get(i);
            if (g.isAvailable()) {
                g.move(GROUND_MOVE_TO_LEFT);

                if (g.isShown(width, height)) {
                    g.draw(canvas);
                }
            } else {
                groundList.remove(g);
                i--;//これいる？
            }
        }

        angel.move();
        angel.draw(canvas);

        //ジャンプゲージを表示
        if(touchDownStartTime > 0){
            float elapsedTime = System.currentTimeMillis() - touchDownStartTime;
            canvas.drawRect(0,0,width * (elapsedTime / MAX_TOUCH_TIME),POWER_GAUGE_HEIGHT,PAINT_POWER_GAUGE);
        }

    }

    /**
     * 地面との距離を返す関数の実装
     *
     * @param angel
     * @return 地面からの距離
     */
    @Override
    public int getDistanceFromGround(Angel angel) {
        int width = getWidth();
        int height = getHeight();

        for (Ground g : groundList) {
            if (!g.isShown(width, height)) {
                continue;
            }
            boolean horizontal
                    = !(angel.rect.left >= g.rect.right || angel.rect.right <= g.rect.left);
            //キャラのいる地面を対象にする
            if (horizontal) {
                if(!g.isSolid()){
                    //穴は落ちる
                    return Integer.MAX_VALUE;
                }

                int distance =  g.rect.top - angel.rect.bottom;
                if(distance < 0){
                    //おしまい
                    gameOver();
                    return Integer.MAX_VALUE;
                }
                return distance;
            }
        }
        //どこかに乗ってなければ無限落下
        return Integer.MAX_VALUE;

    }
}
