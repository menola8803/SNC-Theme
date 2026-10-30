package se.snc.theme;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/**
 * Levande bakgrund som visar en stillbild men rapporterar guld som
 * bakgrundens huvudfärg. Telefonens färgtema (Material You) bygger då sin
 * palett på guld, så klockan, snabbinställningarna och knappar blir guldtonade.
 */
public abstract class SncWallpaperService extends WallpaperService {

    static final int GOLD = 0xFFDDAE54;
    static final int GOLD_LIGHT = 0xFFE9C27A;
    static final int NAVY = 0xFF011634;

    protected abstract int imageRes();

    @Override
    public Engine onCreateEngine() {
        return new SncEngine();
    }

    private class SncEngine extends Engine {
        private Bitmap bitmap;
        private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;
            bitmap = BitmapFactory.decodeResource(getResources(), imageRes(), o);
            if (Build.VERSION.SDK_INT >= 27) notifyColorsChanged();
        }

        @Override
        public WallpaperColors onComputeColors() {
            return new WallpaperColors(Color.valueOf(GOLD), Color.valueOf(GOLD_LIGHT), Color.valueOf(NAVY));
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            draw();
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            draw();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) draw();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (bitmap != null) bitmap.recycle();
            bitmap = null;
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c == null) return;
                c.drawColor(NAVY);
                if (bitmap != null) {
                    int cw = c.getWidth(), ch = c.getHeight();
                    int bw = bitmap.getWidth(), bh = bitmap.getHeight();
                    float scale = Math.max(cw / (float) bw, ch / (float) bh);
                    int sw = Math.round(cw / scale), sh = Math.round(ch / scale);
                    int sx = (bw - sw) / 2, sy = (bh - sh) / 2;
                    c.drawBitmap(bitmap, new Rect(sx, sy, sx + sw, sy + sh), new Rect(0, 0, cw, ch), paint);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }
    }

    /** Hemskärm: strandbilden. */
    public static class Home extends SncWallpaperService {
        @Override
        protected int imageRes() { return R.drawable.wp_home; }
    }

    /** Låsskärm: strandbilden med loggan. */
    public static class Lock extends SncWallpaperService {
        @Override
        protected int imageRes() { return R.drawable.wp_lock; }
    }
}
