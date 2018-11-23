package com.eldhopj.mlkitsample.Overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class ReactOverlay extends GraphicOverlay.Graphic {

    private static final String TAG = "ReactOverlay";

    private static final int RECT_COLOR = Color.YELLOW;
    private static final float STROKE_WIDTH= 8.0f;
    private final Paint reactPaint;
    private final Rect bounds;

    public ReactOverlay(GraphicOverlay overlay, Rect bounds) {
        super(overlay);
        this.bounds = bounds;
        reactPaint = new Paint();
        reactPaint.setColor(RECT_COLOR);
        reactPaint.setStyle(Paint.Style.STROKE);
        reactPaint.setStrokeWidth(STROKE_WIDTH);

        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (bounds == null){
            throw new IllegalStateException("Attempting to draw null bounds");

        }
        RectF rectF = new RectF(bounds);
        canvas.drawRect(rectF,reactPaint);
    }
}
