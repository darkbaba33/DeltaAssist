package com.delta.assist;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

public final class Detector {
    public static final class Blob {
        public int cx, cy, pixels;
        Blob(int cx, int cy, int pixels) { this.cx = cx; this.cy = cy; this.pixels = pixels; }
    }

    public static List<Blob> findRedBlobs(Bitmap bmp) {
        final int w = bmp.getWidth();
        final int h = bmp.getHeight();
        final int[] px = new int[w * h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        boolean[] mask = new boolean[w * h];

        for (int i = 0; i < px.length; i++) {
            final int c = px[i];
            final int r = (c >> 16) & 0xFF;
            final int g = (c >> 8) & 0xFF;
            final int b = c & 0xFF;
            if (r >= Config.redMinR && g >= Config.redMinG && g <= Config.redMaxG && b >= Config.redMinB && b <= Config.redMaxB) {
                mask[i] = true;
            }
        }

        List<Blob> out = new ArrayList<>();
        boolean[] visited = new boolean[w * h];
        int[] stack = new int[w * h];

        for (int y0 = 0; y0 < h; y0++) {
            for (int x0 = 0; x0 < w; x0++) {
                final int start = y0 * w + x0;
                if (!mask[start] || visited[start]) continue;
                int sp = 0;
                stack[sp++] = start;
                visited[start] = true;
                long sx = 0, sy = 0;
                int count = 0;

                while (sp > 0) {
                    final int idx = stack[--sp];
                    final int x = idx % w;
                    final int y = idx / w;
                    sx += x; sy += y; count++;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            final int nx = x + dx, ny = y + dy;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                            final int ni = ny * w + nx;
                            if (mask[ni] && !visited[ni]) { visited[ni] = true; stack[sp++] = ni; }
                        }
                    }
                }
                if (count >= Config.MIN_BLOB_PIXELS) out.add(new Blob((int)(sx / count), (int)(sy / count), count));
            }
        }
        return out;
    }

    public static Blob nearestToCenter(List<Blob> blobs, int screenW, int screenH, int capW, int capH) {
        if (blobs.isEmpty()) return null;
        final float cx = capW / 2f, cy = capH / 2f;
        final float fovPx = capW * Config.fovFraction;
        Blob best = null;
        float bestD = Float.MAX_VALUE;
        for (Blob b : blobs) {
            final float dx = b.cx - cx, dy = b.cy - cy;
            final float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d > fovPx) continue;
            if (d < bestD) { bestD = d; best = b; }
        }
        return best;
    }
}
