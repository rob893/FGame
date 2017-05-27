package com.example.robertherber.fgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Robert Herber on 5/25/2017.
 */

public class TopBorder extends GameObject {
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h){
        height = h;
        width = 20;

        this.x = x;
        this.y = y;

        dx = GamePanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update(){
        x+=dx;
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e){}
    }
}
