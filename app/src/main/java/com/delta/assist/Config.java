package com.delta.assist;

public final class Config {
    private Config() {}
    public static final int CAPTURE_WIDTH = 720;
    public static final int CAPTURE_HEIGHT = 1600;

    // Kırmızı tonlarını en geniş haliyle tarıyoruz
    public static volatile int redMinR = 120;
    public static volatile int redMaxG = 100;
    public static volatile int redMaxB = 100;
    
    // Çok küçük pikselleri (uzaktaki düşmanları) yakalamak için
    public static final int MIN_BLOB_PIXELS = 10; 

    public static volatile boolean aimEnabled = false;
    public static volatile boolean espEnabled = true;
    public static volatile float fovFraction = 0.90f; // Ekranın %90'ını tarar
    public static volatile long cooldownMs = 60;
    public static final long GESTURE_MS = 30;
    public static final float SWIPE_GAIN = 0.85f;
}
