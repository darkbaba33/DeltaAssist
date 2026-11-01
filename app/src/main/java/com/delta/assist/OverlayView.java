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
    private final Paint crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private volatile int capW = 1, capH = 1;
    private volatile int screenW, screenH;
    private volatile List<Detector.Blob> targets = new ArrayList<>();

    public OverlayView(Context ctx, int sw, int sh) {
        super(ctx);
        this.screenW = sw; this.screenH = sh;
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f); 
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f); 
        linePaint.setColor(Color.RED);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(35f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setShadowLayer(5f, 3f, 3f, Color.BLACK);

        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(5f);
        crosshairPaint.setColor(Color.GREEN);
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
        
        // EKRANIN ORTASINA HER ZAMAN YEŞİL HALKAYI ÇİZ
        c.drawCircle(screenW / 2f, screenH / 2f, 40f, crosshairPaint);

        if (targets == null || targets.isEmpty() || !Config.espEnabled) return;
        
        boxPaint.setColor(Config.themeColor);
        final float sx = (float) screenW / capW;
        final float sy = (float) screenH / capH;

        for (Detector.Blob b : targets) {
            final float x = b.cx * sx;
            final float y = b.cy * sy;
            Rect rect = new Rect((int)(x - 50), (int)(y - 100), (int)(x + 50), (int)(y + 100));

            if (Config.showBox) c.drawRect(rect, boxPaint);
            if (Config.showLine) c.drawLine(screenW / 2f, screenH, x, y, linePaint);
            if (Config.showName) c.drawText("HEDEF", x - 40f, rect.top - 15f, textPaint);
        }
    }
}
