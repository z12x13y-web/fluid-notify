package io.github.fluidnotify;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XposedHelpers;

/* loaded from: classes.dex */
final class SmallWindowSurface extends Drawable {
    private static final int PANEL_COLOR = -1;
    private boolean attached;
    private final View card;
    private boolean closed;
    private final float originalAlpha;
    private final ViewGroup root;
    private Bitmap snapshot;
    private final Paint paint = new Paint(3);
    private final RectF rect = new RectF();
    private final RectF contentRect = new RectF();
    private final Path clip = new Path();
    private final float[] radii = new float[8];
    private final int[] rootLocation = new int[2];
    private float panelAlpha = 1.0f;
    private float contentAlpha = 1.0f;

    SmallWindowSurface(View view, ViewGroup viewGroup, Object obj) {
        this.card = view;
        this.root = viewGroup;
        this.originalAlpha = view.getAlpha();
        float min = Math.min(1.0f, 1024.0f / Math.max(view.getWidth(), view.getHeight()));
        this.snapshot = Bitmap.createBitmap(Math.max(1, Math.round(view.getWidth() * min)), Math.max(1, Math.round(view.getHeight() * min)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.snapshot);
        canvas.scale(min, min);
        view.draw(canvas);
        update(obj);
    }

    private static float clamp(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    private static float value(Object obj, String str) {
        return ((Number) XposedHelpers.callMethod(obj, str, new Object[0])).floatValue();
    }

    void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.attached) {
            this.root.getOverlay().remove(this);
            this.card.setAlpha(this.originalAlpha);
        }
        this.snapshot = null;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.closed || this.snapshot == null || this.rect.isEmpty()) {
            return;
        }
        int save = canvas.save();
        this.clip.reset();
        this.clip.addRoundRect(this.rect, this.radii, Path.Direction.CW);
        canvas.clipPath(this.clip);
        this.paint.setColor(PANEL_COLOR);
        this.paint.setAlpha(Math.round(this.panelAlpha * 255.0f * this.originalAlpha));
        canvas.drawRect(this.rect, this.paint);
        if (this.contentAlpha > 0.0f) {
            float min = Math.min(this.rect.width() / this.snapshot.getWidth(), this.rect.height() / this.snapshot.getHeight());
            float width = (this.snapshot.getWidth() * min) / 2.0f;
            float height = (this.snapshot.getHeight() * min) / 2.0f;
            this.contentRect.set(this.rect.centerX() - width, this.rect.centerY() - height, this.rect.centerX() + width, this.rect.centerY() + height);
            this.paint.setAlpha(Math.round(this.panelAlpha * 255.0f * this.contentAlpha * this.originalAlpha));
            canvas.drawBitmap(this.snapshot, (Rect) null, this.contentRect, this.paint);
        }
        canvas.restoreToCount(save);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.paint.setColorFilter(colorFilter);
    }

    void start() {
        if (this.closed || this.attached) {
            return;
        }
        this.attached = true;
        this.root.getOverlay().add(this);
        this.card.setAlpha(0.0f);
    }

    void update(Object obj) {
        if (this.closed) {
            return;
        }
        this.root.getLocationOnScreen(this.rootLocation);
        this.rect.set(value(obj, "getLeft") - this.rootLocation[0], value(obj, "getTop") - this.rootLocation[1], value(obj, "getRight") - this.rootLocation[0], value(obj, "getBottom") - this.rootLocation[1]);
        float max = Math.max(0.0f, value(obj, "getTopCornerRadius"));
        float max2 = Math.max(0.0f, value(obj, "getBottomCornerRadius"));
        int i = 0;
        while (i < 8) {
            this.radii[i] = i < 4 ? max : max2;
            i++;
        }
        this.panelAlpha = clamp(value(obj, "getAlpha"));
        this.contentAlpha = clamp(value(obj, "getContentAlpha"));
        setBounds(0, 0, this.root.getWidth(), this.root.getHeight());
        invalidateSelf();
    }
}
