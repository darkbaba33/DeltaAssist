package com.delta.assist;

public final class Config {
    private Config() {}
    public static final int CAPTURE_WIDTH = 720;
    public static final int CAPTURE_HEIGHT = 1600;
    public static volatile int redMinR = 180;
    public static volatile int redMinG = 0;
    public static volatile int redMaxG = 90;
    public static volatile int redMinB = 0;
    public static volatile int redMaxB = 90;
    public static final int MIN_BLOB_PIXELS = 24;
    public static volatile boolean aimEnabled = false;
    public static volatile boolean espEnabled = true;
    public static volatile float fovFraction = 0.30f;
    public static volatile long cooldownMs = 90;
    public static final long GESTURE_MS = 40;
    public static final float SWIPE_GAIN = 0.85f;
}
