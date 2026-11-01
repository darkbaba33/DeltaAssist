package com.delta.assist;

public final class Config {
    private Config() {}
    public static final int CAPTURE_WIDTH = 720;
    public static final int CAPTURE_HEIGHT = 1600;
    
    // Delta Force'un düşman isim etiketleri genellikle parlak kırmızıdır.
    public static volatile int redMinR = 180; 
    public static volatile int redMaxG = 80;
    public static volatile int redMaxB = 80;
    
    public static final int MIN_BLOB_PIXELS = 15; 
    
    public static volatile boolean espEnabled = true;
    public static volatile boolean showBox = true;
    public static volatile boolean showLine = true;
    public static volatile boolean showName = true;
    public static volatile boolean showSkeleton = false; 
    
    public static volatile boolean aimEnabled = false;
    public static volatile float fovFraction = 0.90f;
    public static volatile long cooldownMs = 60;
    public static final long GESTURE_MS = 30;
    public static final float SWIPE_GAIN = 0.85f;
    public static volatile int themeColor = 0xFFD500F9; // Neon Mor
}
