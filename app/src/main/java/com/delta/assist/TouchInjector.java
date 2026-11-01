package com.delta.assist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;

public final class TouchInjector {
    private final AccessibilityService svc;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile long lastFire = 0L;

    public TouchInjector(AccessibilityService svc) { this.svc = svc; }

    public void swipe(float cx, float cy, float tx, float ty) {
        final long now = System.currentTimeMillis();
        if (now - lastFire < Config.cooldownMs) return;
        lastFire = now;
        final float gain = Config.SWIPE_GAIN;
        final float ex = cx + (tx - cx) * gain;
        final float ey = cy + (ty - cy) * gain;
        final Path path = new Path();
        path.moveTo(cx, cy);
        path.lineTo(ex, ey);
        final GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, Config.GESTURE_MS);
        final GestureDescription desc = new GestureDescription.Builder().addStroke(stroke).build();
        handler.post(() -> svc.dispatchGesture(desc, null, null));
    }
}
