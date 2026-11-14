package io.github.fluidnotify;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

/* loaded from: classes.dex */
final class CapsuleMarquee extends View {
    private final float density;
    private final TextPaint paint;
    private long started;
    private String text;
    private StaticLayout textLayout;
    private float textWidth;

    CapsuleMarquee(Context context) {
        super(context);
        TextPaint textPaint = new TextPaint(1);
        this.paint = textPaint;
        this.text = "";
        this.density = getResources().getDisplayMetrics().density;
        textPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 12.0f);
        textPaint.setColor(-1);
        setImportantForAccessibility(1);
    }

    private void drawLayout(Canvas canvas, float f, float f2) {
        int save = canvas.save();
        canvas.translate(f, f2);
        this.textLayout.draw(canvas);
        canvas.restoreToCount(save);
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.started == 0) {
            this.started = SystemClock.uptimeMillis();
        }
        invalidate();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        if (width <= 0 || this.text.isEmpty() || this.textLayout == null) {
            return;
        }
        boolean z = this.textWidth > ((float) width);
        float f = this.density * 28.0f;
        float max = z ? (z ? ((Math.max(0L, (SystemClock.uptimeMillis() - this.started) - 1000) * this.density) * 24.0f) / 1000.0f : 0.0f) % (this.textWidth + f) : 0.0f;
        int save = canvas.save();
        canvas.clipRect(0, 0, width, getHeight());
        float height = (getHeight() - this.textLayout.getHeight()) / 2.0f;
        float f2 = -max;
        drawLayout(canvas, f2, height);
        if (z) {
            drawLayout(canvas, f2 + this.textWidth + f, height);
        }
        canvas.restoreToCount(save);
        if (z && isAttachedToWindow() && isShown() && getWindowVisibility() == 0) {
            postInvalidateDelayed(33L);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(TextView.class.getName());
        accessibilityNodeInfo.setText(this.text);
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            invalidate();
        }
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i == 0) {
            invalidate();
        }
    }

    void setText(String str) {
        if (this.text.equals(str)) {
            return;
        }
        this.text = str;
        StaticLayout build = StaticLayout.Builder.obtain(str, 0, str.length(), this.paint, Math.max(1, ((int) Math.ceil(Layout.getDesiredWidth(str, this.paint))) + 1)).setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false).setUseLineSpacingFromFallbacks(true).build();
        this.textLayout = build;
        this.textWidth = build.getLineWidth(0);
        this.started = SystemClock.uptimeMillis();
        setContentDescription(str);
        invalidate();
    }
}
