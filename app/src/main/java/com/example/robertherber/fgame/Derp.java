package com.example.robertherber.fgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by Robert Herber on 5/26/2017.
 */

public class Derp extends GameObject {
    private Bitmap image;
    private boolean goingUp = true;
    int speed;
    private Random rand = new Random();

    public Derp(Bitmap res, int x, int y, int w, int h){
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        speed = 1+ rand.nextInt(4);
        dx = GamePanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void setGoingUp(boolean b){
        goingUp = b;
    }

    public void update(){
        x += dx;
        if(goingUp){
            y+= speed;
        } else {
            y -= speed;
        }
        /*if(y >= GamePanel.HEIGHT){
            goingUp = false;
        }
        if(y <= 0){
            goingUp = true;
        }*/
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e){}
    }
}
