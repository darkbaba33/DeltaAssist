package com.delta.assist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CaptureService extends Service {
    public static final String EXTRA_RESULT_CODE = "rc";
    public static final String EXTRA_RESULT_DATA = "ri";
    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private volatile boolean running;
    private OverlayView overlay;
    private MenuView menuView;
    private WindowManager wm;
    private int screenW, screenH, dpi, capW, capH;
    private long lastFire;
    private final Bitmap reuseBuffer = Bitmap.createBitmap(Config.CAPTURE_WIDTH, Config.CAPTURE_HEIGHT, Bitmap.Config.ARGB_8888);

    private final MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override public void onStop() { super.onStop(); running = false; }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

        Intent data = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data = intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent.class);
        } else {
            data = intent.getParcelableExtra(EXTRA_RESULT_DATA);
        }
        final int rc = intent.getIntExtra(EXTRA_RESULT_CODE, 0);

        if (data == null || rc == 0) { stopSelf(); return START_NOT_STICKY; }

        handlerThread = new HandlerThread("CaptureThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        MediaProjectionManager mgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        projection = mgr.getMediaProjection(rc, data);
        if (projection == null) { stopSelf(); return START_NOT_STICKY; }

        projection.registerCallback(projectionCallback, backgroundHandler);

        try {
            WindowManager w = (WindowManager) getSystemService(WINDOW_SERVICE);
            screenW = w.getCurrentWindowMetrics().getBounds().width();
            screenH = w.getCurrentWindowMetrics().getBounds().height();
        } catch (Exception ignored) {}

        if (screenW == 0 || screenH == 0) {
            screenW = getResources().getDisplayMetrics().widthPixels;
            screenH = getResources().getDisplayMetrics().heightPixels;
        }
        dpi = getResources().getDisplayMetrics().densityDpi;
        capW = Config.CAPTURE_WIDTH;
        capH = (int) ((long) capW * screenH / Math.max(1, screenW));

        reader = ImageReader.newInstance(capW, capH, PixelFormat.RGBA_8888, 2);
        display = projection.createVirtualDisplay("delta-cap", capW, capH, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, backgroundHandler);

        setupOverlay();
        running = true;
        reader.setOnImageAvailableListener(this::onFrame, backgroundHandler);
        return START_STICKY;
    }

    private void setupOverlay() {
        try {
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            overlay = new OverlayView(this, screenW, screenH);
            WindowManager.LayoutParams lpEsp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            wm.addView(overlay, lpEsp);

            // BURASI DÜZELTİLDİ: Menü genişliği 800 piksel yapıldı ki yazılar dikey olmasın
            WindowManager.LayoutParams lpMenu = new WindowManager.LayoutParams(800, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
            lpMenu.gravity = Gravity.TOP | Gravity.START;
            lpMenu.x = 50; lpMenu.y = 200;
            menuView = new MenuView(this, wm, lpMenu);
            wm.addView(menuView, lpMenu);
        } catch (Exception ignored) {}
    }

    private void onFrame(ImageReader r) {
        if (!running) return;
        Image img = null;
        try {
            img = r.acquireLatestImage();
            if (img == null) return;
            Image.Plane plane = img.getPlanes()[0];
            ByteBuffer buf = plane.getBuffer();
            if (plane.getPixelStride() == 4 && plane.getRowStride() == capW * 4) {
                buf.rewind(); reuseBuffer.copyPixelsFromBuffer(buf);
            } else {
                buf.rewind(); int[] out = new int[capW * capH]; byte[] row = new byte[plane.getRowStride()];
                for (int y = 0; y < capH; y++) {
                    buf.get(row, 0, Math.min(plane.getRowStride(), buf.remaining()));
                    for (int x = 0; x < capW; x++) {
                        int b = row[x * plane.getPixelStride()] & 0xFF;
                        int g = row[x * plane.getPixelStride() + 1] & 0xFF;
                        int rr = row[x * plane.getPixelStride() + 2] & 0xFF;
                        int a = row[x * plane.getPixelStride() + 3] & 0xFF;
                        out[y * capW + x] = (a << 24) | (rr << 16) | (g << 8) | b;
                    }
                }
                reuseBuffer.setPixels(out, 0, capW, 0, 0, capW, capH);
            }

            List<Detector.Blob> allBlobs = Detector.findRedBlobs(reuseBuffer);
            List<Detector.Blob> inFov = new ArrayList<>();
            for (Detector.Blob b : allBlobs) {
                float dx = b.cx - (capW / 2f), dy = b.cy - (capH / 2f);
                if (Math.sqrt(dx*dx + dy*dy) < capW * Config.fovFraction) inFov.add(b);
            }

            if (overlay != null) {
                if (Config.espEnabled) overlay.post(() -> overlay.render(inFov, capW, capH, screenW, screenH));
                else overlay.post(() -> overlay.render(new ArrayList<>(), capW, capH, screenW, screenH));
            }
        } catch (Exception ignored) { } finally { if (img != null) img.close(); }
    }

    private Notification buildNotification() {
        String ch = "delta";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(new NotificationChannel(ch, "Buse", NotificationManager.IMPORTANCE_LOW));
        }
        return new Notification.Builder(this, ch).setContentTitle("Buse naz delta force").setContentText("Aktif").setSmallIcon(android.R.drawable.ic_menu_view).build();
    }

    @Override
    public void onDestroy() {
        running = false;
        if (handlerThread != null) handlerThread.quitSafely();
        if (display != null) display.release();
        if (reader != null) reader.close();
        if (projection != null) projection.stop();
        if (overlay != null && wm != null) try { wm.removeView(overlay); } catch (Exception ignored) { }
        if (menuView != null && wm != null) try { wm.removeView(menuView); } catch (Exception ignored) { }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
