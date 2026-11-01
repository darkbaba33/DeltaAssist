package com.delta.assist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint skeletonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private volatile int capW = 1, capH = 1;
    private volatile int screenW, screenH;
    private volatile List<Detector.Blob> targets = new ArrayList<>();

    public OverlayView(Context ctx, int sw, int sh) {
        super(ctx);
        this.screenW = sw; this.screenH = sh;
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        linePaint.setColor(Color.RED);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(4f, 2f, 2f, Color.BLACK);
        skeletonPaint.setStyle(Paint.Style.STROKE);
        skeletonPaint.setStrokeWidth(3f);
    }

    public void render(List<Detector.Blob> bList, int cw, int ch, int sw, int sh) {
        this.targets = bList;
        this.capW = cw; this.capH = ch;
        this.screenW = sw; this.screenH = sh;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (targets == null || targets.isEmpty() || !Config.espEnabled) return;
        
        boxPaint.setColor(Config.themeColor);
        skeletonPaint.setColor(Config.themeColor);

        final float sx = (float) screenW / capW;
        final float sy = (float) screenH / capH;

        for (Detector.Blob b : targets) {
            final float x = b.cx * sx;
            final float y = b.cy * sy;
            float boxWidth = 120f;
            float boxHeight = 200f;
            Rect rect = new Rect((int)(x - boxWidth/2), (int)(y - boxHeight*0.7f), (int)(x + boxWidth/2), (int)(y + boxHeight*0.3f));

            if (Config.showBox) c.drawRect(rect, boxPaint);
            if (Config.showLine) c.drawLine(screenW / 2f, screenH, x, y, linePaint);
            if (Config.showName) c.drawText("DUSMAN", x - 45f, rect.top - 15f, textPaint);

            if (Config.showSkeleton) {
                float headY = rect.top + 30;
                c.drawCircle(x, headY, 15f, skeletonPaint);
                c.drawLine(x, headY + 15, x, headY + 80, skeletonPaint);
                c.drawLine(x - 30, headY + 40, x + 30, headY + 40, skeletonPaint);
                c.drawLine(x, headY + 80, x - 25, headY + 150, skeletonPaint);
                c.drawLine(x, headY + 80, x + 25, headY + 150, skeletonPaint);
            }
        }
    }
}
