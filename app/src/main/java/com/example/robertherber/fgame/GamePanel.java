package com.example.robertherber.fgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Robert Herber on 5/24/2017.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private long derpStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private ArrayList<Derp> derps;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private int progressDenom = 20; //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;
    private Bitmap scaledDerp;
    //private SoundPool sp;
    //private int[] soundIds;
    private MediaPlayer mediaPlayer;

    public GamePanel(Context context){
        super(context);

        getHolder().addCallback(this);



        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter < 1000){
            counter++;
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch(InterruptedException e){e.printStackTrace();}
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        derps = new ArrayList<Derp>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();
        derpStartTime = System.nanoTime();
        thread = new MainThread(getHolder(), this);
        scaledDerp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.derp), 75, 75, true);
        //sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        //soundIds = new int[10];
        //soundIds[0] = sp.load(getContext(), R.raw.suck, 1);
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.suck);

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()== MotionEvent.ACTION_DOWN){
            if(!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying()){
                if(!started){
                    started = true;
                }
                reset = false;
                player.setUp(true);
            }
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update(){
        if(player.getPlaying()) {
            if(botborder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            bg.update();
            player.update();

            //calculate the threshold of height the border can have based on score
            //max and min border heart are updated
            //min is met
            maxBorderHeight = 30 + player.getScore()/progressDenom;
            //cap max border height so that borders can only take up half the screen
            if(maxBorderHeight > HEIGHT/4){
                maxBorderHeight = HEIGHT/4;
            }
            minBorderHeight = 5+player.getScore()/progressDenom;

            //check top border collision
            for(int i = 0; i<topborder.size(); i++){
                if(collision(topborder.get(i), player)){
                    player.setPlaying(false);
                }
            }
            //check bottom border collision
            for(int i = 0; i<botborder.size(); i++){
                if(collision(botborder.get(i), player)){
                    player.setPlaying(false);
                }
            }
            //create top border
            this.updateTopBorder();
            //create bottom border
            this.updateBottomBorder();
            //add missiles on timer
            long missilesElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if(player.getScore() > 100 && missilesElapsed > (100 - player.getScore()/4)){
                missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10,
                        (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+ maxBorderHeight), 45, 15, player.getScore(), 13));
            }
            if(missilesElapsed > (1250 - player.getScore()/4)){
                //System.out.println("making missiles");
                //System.out.println(missiles.size());
                //first missile always goes down middle
                /*if(missiles.size() == 0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10, HEIGHT/2, 45, 15, player.getScore(), 13));
                } else {*/
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10,
                            (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+ maxBorderHeight), 45, 15, player.getScore(), 13)); //always generated within borders
                //}
                //reset timer
                missileStartTime = System.nanoTime();
            }

            for(int i = 0; i<missiles.size(); i++){

                missiles.get(i).update();

                if(collision(missiles.get(i), player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                if(missiles.get(i).getX()<-100){
                    missiles.remove(i);
                    break;
                }
            }

            //add derps

            long derpsElapsed = (System.nanoTime() - derpStartTime) / 1000000;
            if(derpsElapsed > 2500){
                derps.add(new Derp(scaledDerp, WIDTH +10,
                        (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+ maxBorderHeight), 75, 75));
                derpStartTime = System.nanoTime();
            }

            for(int i = 0; i <derps.size(); i++){
                derps.get(i).update();
                //if(derps.get(i).getY() >= HEIGHT){
                //    derps.get(i).setGoingUp(false);
                //}
                /*if(derps.get(i).getY() <= 0){
                    derps.get(i).setGoingUp(true);
                }*/
                for(int j = 0; j<topborder.size(); j++){
                    if(collision(derps.get(i), topborder.get(j))){
                        derps.get(i).setGoingUp(true);
                    }
                }

                for(int j = 0; j<botborder.size(); j++) {
                    if (collision(derps.get(i), botborder.get(j))) {
                        derps.get(i).setGoingUp(false);
                    }
                }
                if(collision(derps.get(i), player)){
                    player.setPlaying(false);
                    break;
                }
                if(derps.get(i).getX()<-100){
                    derps.remove(i);
                    break;
                }
            }

            //add smoke puffs
            long elapsed = (System.nanoTime() - smokeStartTime) /1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size(); i++){
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else{
            player.resetDY();
            if(!reset){
                //sp.play(soundIds[0], 1, 1, 1, 0, 1);
                mediaPlayer.start();
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(), player.getY()-30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if(resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b){
        if(Rect.intersects(a.getRectangle(), b.getRectangle())){
            return true;
        }
        return false;
    }

    @Override
    public  void draw(Canvas canvas){
        final float scaleFactorX = getWidth() / (WIDTH*1.f);
        final float scaleFactorY = getHeight() / (HEIGHT*1.f);

        if(canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            //draw background
            bg.draw(canvas);

            //draw player
            if(!dissapear) {
                player.draw(canvas);
            }

            //draw smokepuffs
            for(Smokepuff sp: smoke){
                sp.draw(canvas);
            }
            //draw missiles
            for(Missile m: missiles){
                m.draw(canvas);
            }

            //draw derps
            for(Derp d: derps){
                d.draw(canvas);
            }

            //draw topBorder
            for(TopBorder tb: topborder){
                tb.draw(canvas);
            }

            for(BotBorder bb: botborder){
                bb.draw(canvas);
            }

            //draw explosion
            if(started){
                explosion.draw(canvas);
            }

            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        //every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore() % 50 == 0){
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,
                    0, (int)((rand.nextDouble()*(maxBorderHeight))+1)));
        }
        for(int i = 0; i <topborder.size(); i++){
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20){
                topborder.remove(i);
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight){
                    topDown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight() <= minBorderHeight){
                    topDown = true;
                }
                if(topDown){
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()+1));
                } else {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder(){
        //every 40 points, insert randmomly placed bottom blocks that break pattern
        if(player.getScore() % 40 == 0){
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size()-1).getX()+20,
                    (int)((rand.nextDouble()*maxBorderHeight)+(HEIGHT-maxBorderHeight))));
        }
        for(int i = 0; i<botborder.size(); i++){
            botborder.get(i).update();

            if(botborder.get(i).getX() <-20) {
                botborder.remove(i);

                if (botborder.get(botborder.size() - 1).getHeight() >= maxBorderHeight) {
                    botDown = false;
                }
                if (botborder.get(botborder.size() - 1).getHeight() <= minBorderHeight) {
                    botDown = true;
                }

                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() - 1));
                }
            }
        }
    }

    public void newGame(){
        dissapear = false;
        botborder.clear();
        topborder.clear();
        missiles.clear();
        smoke.clear();
        derps.clear();


        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.resetDY();

        player.resetScore();
        player.setY(HEIGHT/2);

        if(player.getScore() > best){
            best = player.getScore();
        }

        //create initial borders

        for(int i = 0; i*20<WIDTH+40; i++){
            if(i==0){
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, 0, 10));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, 0, topborder.get(i-1).getHeight()+1));
            }
        }
        //initial botborder
        for(int i = 0; i*20<WIDTH+40; i++){
            if(i==0) {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i * 20, HEIGHT - minBorderHeight));
            }
            else{
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, botborder.get(i-1).getY()-1));
            }
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()*3), 10, HEIGHT -10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if(!player.getPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }

    }
}
