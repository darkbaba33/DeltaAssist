package com.delta.assist;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class AimService extends AccessibilityService {
    private static volatile AimService instance;
    private TouchInjector injector;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        injector = new TouchInjector(this);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public boolean onUnbind(Intent intent) { instance = null; return super.onUnbind(intent); }
    public static AimService get() { return instance; }
    public TouchInjector injector() { return injector; }
}
