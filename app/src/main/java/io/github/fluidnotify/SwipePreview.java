package io.github.fluidnotify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/* loaded from: classes.dex */
final class SwipePreview extends View {
    private boolean allowed;
    private ValueAnimator animator;
    private final RectF card;
    private final Path clip;
    private final float density;
    private int distance;
    private float downX;
    private float downY;
    private float dragStartOffset;
    private boolean dragging;
    private int excluded;
    private boolean expanded;
    private float expansion;
    private ValueAnimator expansionAnimator;
    private boolean interrupted;
    private float lightX;
    private float lightY;
    private final Typeface medium;
    private final RectF moved;
    private final Typeface normal;
    private float offset;
    private final Paint paint;
    private boolean ready;
    private LensRenderer renderer;
    private boolean restoring;
    private Bitmap scenery;
    private final RectF stage;
    private String status;
    private final TextPaint textPaint;
    private boolean vertical;

    private static final class LensRenderer {
        private final Paint paint = new Paint(1);
        private final RuntimeShader shader;

        LensRenderer(Context context, Bitmap bitmap, float f) throws IOException {
            InputStream openRawResource = context.getResources().openRawResource(R.raw.liquid_glass);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[4096];
                    while (true) {
                        int read = openRawResource.read(bArr);
                        if (read == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write(bArr, 0, read);
                        }
                    }
                    RuntimeShader runtimeShader = new RuntimeShader(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
                    this.shader = runtimeShader;
                    byteArrayOutputStream.close();
                    if (openRawResource != null) {
                        openRawResource.close();
                    }
                    BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    bitmapShader.setFilterMode(2);
                    runtimeShader.setInputShader("scenery", bitmapShader);
                    runtimeShader.setFloatUniform("resolution", bitmap.getWidth(), bitmap.getHeight());
                    runtimeShader.setFloatUniform("density", f);
                    this.paint.setShader(runtimeShader);
                } finally {
                }
            } catch (Throwable th) {
                if (openRawResource != null) {
                    try {
                        openRawResource.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }

        void draw(Canvas canvas, RectF rectF, RectF rectF2, float f, float f2, float f3) {
            this.shader.setFloatUniform("lens", rectF2.left, rectF2.top, rectF2.right, rectF2.bottom);
            this.shader.setFloatUniform("radius", f);
            this.shader.setFloatUniform("light", f2, f3);
            canvas.drawRect(rectF, this.paint);
        }
    }

    SwipePreview(Context context) {
        super(context);
        this.paint = new Paint(3);
        this.textPaint = new TextPaint(1);
        this.card = new RectF();
        this.moved = new RectF();
        this.stage = new RectF();
        this.clip = new Path();
        this.normal = Typeface.create("sans-serif", 0);
        this.medium = Typeface.create("sans-serif-medium", 0);
        this.expansion = 1.0f;
        this.excluded = 15;
        this.distance = 50;
        this.expanded = true;
        this.status = "试试左右边缘；中间起手不会移动";
        this.density = getResources().getDisplayMetrics().density;
        setClickable(true);
        setFocusable(true);
        setImportantForAccessibility(1);
    }

    private void cancelAnimation() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.animator = null;
        }
    }

    private float dp(float f) {
        return f * this.density;
    }

    private void drawFallback(Canvas canvas) {
        canvas.drawBitmap(this.scenery, 0.0f, 0.0f, this.paint);
        this.paint.setColor(340880763);
        canvas.drawRoundRect(this.moved.left, this.moved.top + dp(5.0f), this.moved.right, this.moved.bottom + dp(5.0f), dp(25.0f), dp(25.0f), this.paint);
        canvas.save();
        this.clip.rewind();
        this.clip.addRoundRect(this.moved, dp(25.0f), dp(25.0f), Path.Direction.CW);
        canvas.clipPath(this.clip);
        canvas.scale(1.035f, 1.035f, this.moved.centerX(), this.moved.centerY());
        this.paint.setColor(-1);
        canvas.drawBitmap(this.scenery, 0.0f, 0.0f, this.paint);
        canvas.drawColor(1224736767);
        canvas.restore();
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(dp(1.3f));
        this.paint.setColor(-419430401);
        canvas.drawRoundRect(this.moved, dp(25.0f), dp(25.0f), this.paint);
        this.paint.setStyle(Paint.Style.FILL);
    }

    private void drawNotification(Canvas canvas) {
        canvas.save();
        canvas.translate(this.offset, 0.0f);
        if (this.excluded > 0) {
            float min = Math.min((this.card.width() * this.excluded) / 100.0f, this.card.width() / 2.0f);
            this.paint.setColor(-1439134262);
            this.paint.setStrokeWidth(dp(2.0f));
            this.paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(dp(24.0f) + this.card.left, dp(5.0f) + this.card.top, Math.max(this.card.left + dp(24.0f), this.card.left + min), dp(5.0f) + this.card.top, this.paint);
            canvas.drawLine(Math.min(this.card.right - dp(24.0f), this.card.right - min), dp(5.0f) + this.card.top, this.card.right - dp(24.0f), dp(5.0f) + this.card.top, this.paint);
            this.paint.setStrokeCap(Paint.Cap.BUTT);
        }
        float dp = this.card.left + dp(15.0f);
        float dp2 = this.card.top + dp(17.0f);
        this.paint.setColor(-1191182337);
        canvas.drawRoundRect(dp, dp2, dp + dp(31.0f), dp2 + dp(31.0f), dp(11.0f), dp(11.0f), this.paint);
        this.paint.setColor(-14254723);
        canvas.drawCircle(dp(12.0f) + dp, dp(18.0f) + dp2, dp(5.0f), this.paint);
        canvas.drawCircle(dp(17.0f) + dp, dp(14.0f) + dp2, dp(6.0f), this.paint);
        canvas.drawCircle(dp(22.0f) + dp, dp(18.0f) + dp2, dp(4.0f), this.paint);
        canvas.drawRoundRect(dp + dp(9.0f), dp2 + dp(17.0f), dp + dp(25.0f), dp2 + dp(23.0f), dp(3.0f), dp(3.0f), this.paint);
        label(canvas, "通知流体云", dp + dp(42.0f), dp2 + dp(14.0f), 13, -14663864, false, this.card.width() - dp(112.0f));
        label(canvas, "现在", this.card.right - dp(29.0f), dp2 + dp(12.0f), 10, -11374214, true, dp(30.0f));
        label(canvas, "一条刚刚好的提醒", dp + dp(42.0f), dp2 + dp(31.0f), 10, -12227981, false, this.card.width() - dp(74.0f));
        if (this.expansion > 0.01f) {
            canvas.save();
            canvas.clipRect(this.card);
            int round = Math.round(this.expansion * 255.0f) << 24;
            this.textPaint.setTypeface(this.medium);
            label(canvas, "新的消息，轻盈浮现", dp, this.card.top + dp(81.0f), 16, round | 2113352, false, this.card.width() - dp(30.0f));
            this.textPaint.setTypeface(this.normal);
            label(canvas, "轻触收起 · 滑动试试玻璃的流动", dp, this.card.top + dp(105.0f), 11, round | 4549235, false, this.card.width() - dp(30.0f));
            canvas.restore();
        }
        canvas.restore();
    }

    private void drawRuler(Canvas canvas) {
        float width = getWidth() / 2.0f;
        float width2 = (getWidth() - dp(48.0f)) / 2.0f;
        float height = getHeight() - dp(53.0f);
        label(canvas, "起手 " + this.excluded + "%", dp(22.0f), height - dp(18.0f), 11, -13214364, false, width2);
        label(canvas, "清除阈值 " + this.distance + "%", (getWidth() - dp(22.0f)) - width2, height - dp(18.0f), 11, -6404526, false, width2);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        this.paint.setStrokeWidth(dp(3.0f));
        this.paint.setColor(1434953135);
        canvas.drawLine(width - width2, height, width + width2, height, this.paint);
        float f = (this.distance * width2) / 60.0f;
        this.paint.setColor(-3052683);
        this.paint.setStrokeWidth(dp(2.0f));
        float f2 = width - f;
        canvas.drawLine(f2, height - dp(5.0f), f2, height + dp(5.0f), this.paint);
        float f3 = width + f;
        canvas.drawLine(f3, height - dp(5.0f), f3, height + dp(5.0f), this.paint);
        float max = Math.max(-1.0f, Math.min(1.0f, this.offset / Math.max(1.0f, this.card.width() * 0.6f)));
        this.paint.setColor(this.ready ? -4368792 : -14451580);
        this.paint.setStrokeWidth(dp(3.0f));
        float f4 = width + (max * width2);
        canvas.drawLine(width, height, f4, height, this.paint);
        this.paint.setStrokeCap(Paint.Cap.BUTT);
        this.paint.setColor(-1);
        canvas.drawCircle(f4, height, dp(5.0f), this.paint);
        this.paint.setColor(-14451580);
        canvas.drawCircle(f4, height, dp(2.5f), this.paint);
        label(canvas, this.status, width, getHeight() - dp(19.0f), 11, -13214364, true, getWidth() - dp(28.0f));
    }

    private void expandImmediately() {
        ValueAnimator valueAnimator = this.expansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.expanded = true;
        this.expansion = 1.0f;
    }

    private String hint() {
        int i = this.excluded;
        return i == 0 ? "已关闭滑动清除，仍可点击展开或收起" : i == 50 ? "任意位置起手；滑过 " + this.distance + "% 后松手清除" : "试试左右边缘；中间起手不会移动";
    }

    private void label(Canvas canvas, String str, float f, float f2, int i, int i2, boolean z, float f3) {
        this.textPaint.setColor(i2);
        this.textPaint.setTextSize(dp(i));
        this.textPaint.setTextAlign(z ? Paint.Align.CENTER : Paint.Align.LEFT);
        CharSequence ellipsize = TextUtils.ellipsize(str, this.textPaint, Math.max(1.0f, f3), TextUtils.TruncateAt.END);
        canvas.drawText(ellipsize, 0, ellipsize.length(), f, f2, this.textPaint);
    }

    private void settle(final boolean z) {
        float copySign = z ? Math.copySign(getWidth(), this.offset) : 0.0f;
        cancelAnimation();
        if (!ValueAnimator.areAnimatorsEnabled()) {
            this.offset = copySign;
            this.status = z ? "模拟清除完成，点击这里重新测试" : "已回弹，卡片保留";
            invalidate();
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.offset, copySign);
        this.animator = ofFloat;
        ofFloat.setDuration(z ? 260L : 420L);
        this.animator.setInterpolator(z ? GlassStyle.EASE : new OvershootInterpolator(0.6f));
        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: io.github.fluidnotify.SwipePreview$$ExternalSyntheticLambda1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipePreview.this.m84lambda$settle$1$iogithubfluidnotifySwipePreview(valueAnimator);
            }
        });
        this.animator.addListener(new AnimatorListenerAdapter() { // from class: io.github.fluidnotify.SwipePreview.1
            boolean cancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.cancelled) {
                    return;
                }
                if (!z) {
                    SwipePreview.this.offset = 0.0f;
                }
                SwipePreview.this.status = z ? "模拟清除完成，点击这里重新测试" : "已回弹，卡片保留";
                SwipePreview.this.invalidate();
            }
        });
        this.animator.start();
    }

    private void updateCard() {
        float dp = dp(16.0f);
        float dp2 = dp(((1.0f - this.expansion) * 25.0f) + 70.0f);
        this.card.set(dp, dp2, Math.max(1.0f + dp, getWidth() - dp), dp((this.expansion * 62.0f) + 64.0f) + dp2);
        this.moved.set(this.card);
        this.moved.offset(this.offset, 0.0f);
    }

    /* renamed from: lambda$performClick$0$io-github-fluidnotify-SwipePreview, reason: not valid java name */
    /* synthetic */ void m83lambda$performClick$0$iogithubfluidnotifySwipePreview(ValueAnimator valueAnimator) {
        this.expansion = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        invalidate();
    }

    /* renamed from: lambda$settle$1$io-github-fluidnotify-SwipePreview, reason: not valid java name */
    /* synthetic */ void m84lambda$settle$1$iogithubfluidnotifySwipePreview(ValueAnimator valueAnimator) {
        this.offset = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        invalidate();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        stopMotion();
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.scenery == null) {
            return;
        }
        updateCard();
        canvas.save();
        this.clip.rewind();
        this.clip.addRoundRect(this.stage, dp(24.0f), dp(24.0f), Path.Direction.CW);
        canvas.clipPath(this.clip);
        this.paint.setColor(-1);
        this.paint.setShader(null);
        if (Build.VERSION.SDK_INT < 33 || this.renderer == null || !canvas.isHardwareAccelerated()) {
            drawFallback(canvas);
        } else {
            this.renderer.draw(canvas, this.stage, this.moved, dp(25.0f), this.lightX, this.lightY);
        }
        label(canvas, "交互预览", dp(18.0f), dp(31.0f), 11, -13214364, false, getWidth() - dp(90.0f));
        this.paint.setColor(1728053247);
        canvas.drawRoundRect(getWidth() - dp(68.0f), dp(15.0f), getWidth() - dp(16.0f), dp(37.0f), dp(11.0f), dp(11.0f), this.paint);
        label(canvas, "仅模拟", getWidth() - dp(42.0f), dp(30.0f), 10, -13214364, true, dp(46.0f));
        drawNotification(canvas);
        drawRuler(canvas);
        canvas.restore();
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        if (i <= 0 || i2 <= 0) {
            return;
        }
        float f = i;
        this.stage.set(0.0f, 0.0f, f, i2);
        this.scenery = GlassScene.create(i, i2);
        this.lightX = f * 0.25f;
        this.lightY = dp(64.0f);
        this.renderer = null;
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                this.renderer = new LensRenderer(getContext(), this.scenery, this.density);
            } catch (IOException | RuntimeException e) {
                Log.w("FluidNotify", "Glass shader unavailable; using translucent lens", e);
            }
        }
        updateCard();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        float x = motionEvent.getX() - this.downX;
        float y = motionEvent.getY() - this.downY;
        if (actionMasked == 0) {
            this.interrupted = false;
            updateCard();
            this.lightX = motionEvent.getX();
            this.lightY = motionEvent.getY();
            if (Math.abs(this.offset) >= getWidth()) {
                cancelAnimation();
                this.offset = 0.0f;
                expandImmediately();
                this.restoring = true;
                this.status = hint();
                invalidate();
                return true;
            }
            if (!this.card.contains(motionEvent.getX() - this.offset, motionEvent.getY())) {
                return false;
            }
            cancelAnimation();
            this.dragStartOffset = this.offset;
            this.downX = motionEvent.getX();
            this.downY = motionEvent.getY();
            this.allowed = SwipeRules.canStart((this.downX - this.offset) - this.card.left, this.card.width(), this.excluded);
            this.dragging = false;
            this.vertical = false;
            this.ready = false;
            return true;
        }
        if (this.restoring) {
            if (actionMasked == 1 || actionMasked == 3) {
                this.restoring = false;
            }
            return true;
        }
        if (actionMasked == 5) {
            this.interrupted = true;
            this.dragging = false;
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            settle(false);
            return true;
        }
        if (this.interrupted) {
            return true;
        }
        if (actionMasked != 2) {
            if (actionMasked != 1 && actionMasked != 3) {
                return true;
            }
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            boolean z = this.dragging;
            if (z && this.allowed) {
                settle(actionMasked == 1 && this.ready);
            } else if (actionMasked == 3 && this.offset != 0.0f) {
                settle(false);
            } else if (!z && !this.vertical && actionMasked == 1) {
                performClick();
            }
            this.dragging = false;
            return true;
        }
        if (!this.dragging && !this.vertical && Math.max(Math.abs(x), Math.abs(y)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
            boolean horizontal = SwipeRules.horizontal(x, y);
            this.dragging = horizontal;
            this.vertical = !horizontal;
            if (horizontal && this.allowed && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        if (this.dragging) {
            if (this.allowed) {
                this.offset = SwipeRules.limit(this.dragStartOffset + x, this.card.width());
                this.lightX = motionEvent.getX();
                this.lightY = motionEvent.getY();
                boolean shouldDismiss = SwipeRules.shouldDismiss(this.offset, this.card.width(), this.distance);
                this.ready = shouldDismiss;
                this.status = shouldDismiss ? "松手模拟清除" : "未达到清除距离，松手回弹";
            } else {
                this.status = this.excluded == 0 ? "滑动清除已关闭" : "请从蓝色标记的边缘起手";
            }
            invalidate();
        }
        return true;
    }

    @Override // android.view.View
    public boolean performClick() {
        super.performClick();
        if (Math.abs(this.offset) >= getWidth()) {
            this.offset = 0.0f;
            expandImmediately();
            this.status = hint();
            invalidate();
            return true;
        }
        this.expanded = !this.expanded;
        ValueAnimator valueAnimator = this.expansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        float f = this.expanded ? 1.0f : 0.0f;
        if (!ValueAnimator.areAnimatorsEnabled()) {
            this.expansion = f;
            invalidate();
            return true;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.expansion, f);
        this.expansionAnimator = ofFloat;
        ofFloat.setDuration(300L);
        this.expansionAnimator.setInterpolator(GlassStyle.EASE);
        this.expansionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: io.github.fluidnotify.SwipePreview$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                SwipePreview.this.m83lambda$performClick$0$iogithubfluidnotifySwipePreview(valueAnimator2);
            }
        });
        this.expansionAnimator.start();
        return true;
    }

    void setBoundary(int i) {
        stopMotion();
        this.excluded = SwipeRules.edgePercent(i);
        expandImmediately();
        int i2 = this.excluded;
        setContentDescription(i2 == 0 ? "通知卡片预览，滑动清除已关闭；点击展开或收起" : i2 == 50 ? "通知卡片预览，整张卡片均可滑动；点击展开或收起" : "通知卡片预览，左右各百分之" + this.excluded + "区域可滑动；点击展开或收起");
        this.status = hint();
        invalidate();
    }

    void setDistance(int i) {
        stopMotion();
        this.distance = SwipeRules.dismissPercent(i);
        expandImmediately();
        this.status = hint();
        invalidate();
    }

    void stopMotion() {
        cancelAnimation();
        ValueAnimator valueAnimator = this.expansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.expansion = this.expanded ? 1.0f : 0.0f;
        this.offset = 0.0f;
        this.dragging = false;
        this.restoring = false;
        this.ready = false;
        this.interrupted = false;
        this.lightX = getWidth() * 0.25f;
        this.lightY = dp(64.0f);
        this.status = hint();
        invalidate();
    }
}
