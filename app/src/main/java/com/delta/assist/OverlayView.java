package com.delta.assist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private volatile int capW = 1, capH = 1;
    private volatile int screenW, screenH;
    private volatile List<Detector.Blob> targets = new ArrayList<>();

    public OverlayView(Context ctx, int sw, int sh) {
        super(ctx);
        this.screenW = sw; this.screenH = sh;
        
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setColor(Color.CYAN);
        
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        linePaint.setColor(Color.RED);
        
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(35f);
        textPaint.setShadowLayer(4f, 2f, 2f, Color.BLACK);
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
        if (targets == null || targets.isEmpty()) return;
        final float sx = (float) screenW / capW;
        final float sy = (float) screenH / capH;

        for (Detector.Blob b : targets) {
            final float x = b.cx * sx;
            final float y = b.cy * sy;
            
            // Düşmanın boyuna göre dinamik kutu boyutu
            float boxWidth = 140f;
            float boxHeight = 220f;

            Rect rect = new Rect(
                (int)(x - boxWidth/2), (int)(y - boxHeight*0.7f), 
                (int)(x + boxWidth/2), (int)(y + boxHeight*0.3f)
            );

            // Kutu ve Snapline
            c.drawRect(rect, boxPaint);
            c.drawLine(screenW / 2f, screenH, x, y, linePaint);
            
            // Üstüne yazı
            c.drawText("HEDEF", x - 40f, rect.top - 15f, textPaint);
        }
    }
}
