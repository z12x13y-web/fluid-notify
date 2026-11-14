package io.github.fluidnotify;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.Property;
import android.view.View;
import android.view.animation.PathInterpolator;

/* loaded from: classes.dex */
final class GlassStyle {
    static final int ACCENT = -15303564;
    static final PathInterpolator EASE = new PathInterpolator(0.2f, 0.8f, 0.2f, 1.0f);
    static final int INK = -15189184;
    static final int MUTED = -10520708;

    static final class Backdrop extends Drawable {
        private Shader blue;
        private Shader mint;
        private final Paint paint = new Paint(1);
        private Shader peach;

        Backdrop() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawColor(-788489);
            this.paint.setShader(this.mint);
            canvas.drawRect(getBounds(), this.paint);
            this.paint.setShader(this.blue);
            canvas.drawRect(getBounds(), this.paint);
            this.paint.setShader(this.peach);
            canvas.drawRect(getBounds(), this.paint);
            this.paint.setShader(null);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            float max = Math.max(1, rect.width());
            float max2 = Math.max(1, rect.height());
            this.mint = new RadialGradient(max * 0.92f, max2 * 0.14f, max * 0.95f, -6759992, 10017224, Shader.TileMode.CLAMP);
            this.blue = new RadialGradient(max * (-0.12f), max2 * 0.48f, max * 0.85f, -5453073, 11324143, Shader.TileMode.CLAMP);
            this.peach = new RadialGradient(max, max2 * 0.93f, max * 0.8f, -995140, 15782076, Shader.TileMode.CLAMP);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }
    }

    private static final class GlassSurface extends Drawable {
        private Shader body;
        private final float line;
        private final float radius;
        private Shader rim;
        private final boolean tinted;
        private final Paint paint = new Paint(1);
        private final RectF outer = new RectF();
        private final RectF inner = new RectF();
        private int alpha = 255;

        GlassSurface(float f, float f2, boolean z) {
            this.radius = f;
            this.line = f2;
            this.tinted = z;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setShader(this.body);
            this.paint.setAlpha(this.alpha);
            RectF rectF = this.outer;
            float f = this.radius;
            canvas.drawRoundRect(rectF, f, f, this.paint);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeWidth(this.line * 1.3f);
            this.paint.setShader(this.rim);
            RectF rectF2 = this.outer;
            float f2 = this.radius;
            canvas.drawRoundRect(rectF2, f2, f2, this.paint);
            this.paint.setShader(null);
            this.paint.setStrokeWidth(this.line * 0.6f);
            this.paint.setColor(1358954495);
            this.paint.setAlpha(Math.round((this.alpha * 80) / 255.0f));
            RectF rectF3 = this.inner;
            float f3 = this.radius;
            float f4 = this.line;
            canvas.drawRoundRect(rectF3, f3 - f4, f3 - f4, this.paint);
            this.paint.setStyle(Paint.Style.FILL);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), this.radius);
            outline.setAlpha(0.35f);
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            this.outer.set(rect);
            RectF rectF = this.outer;
            float f = this.line;
            rectF.inset(f, f);
            this.inner.set(this.outer);
            RectF rectF2 = this.inner;
            float f2 = this.line;
            rectF2.inset(f2 * 1.6f, f2 * 1.6f);
            this.body = new LinearGradient(0.0f, 0.0f, Math.max(1, rect.width()), Math.max(1, rect.height()), this.tinted ? new int[]{-1929379841, 618198510, 1358954495} : new int[]{-1711276033, 1090519039, 1895234559}, (float[]) null, Shader.TileMode.CLAMP);
            this.rim = new LinearGradient(0.0f, 0.0f, Math.max(1, rect.width()), Math.max(1, rect.height()), new int[]{-1, -637534209, 542670472, -570425345}, new float[]{0.0f, 0.35f, 0.7f, 1.0f}, Shader.TileMode.CLAMP);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.alpha = i;
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    GlassStyle() {
    }

    static int dp(Context context, float f) {
        return Math.round(f * context.getResources().getDisplayMetrics().density);
    }

    static void enter(View view, long j) {
        if (ValueAnimator.areAnimatorsEnabled()) {
            view.setAlpha(0.0f);
            view.setTranslationY(dp(view.getContext(), 18.0f));
            view.animate().alpha(1.0f).translationY(0.0f).setStartDelay(j).setDuration(480L).setInterpolator(EASE).withLayer().start();
        }
    }

    static void pressable(View view, int i, int i2) {
        Context context = view.getContext();
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(605453428), rounded(context, i, i2), rounded(context, -1, i2)));
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[]{android.R.attr.state_pressed}, scale(view, 0.97f, 110L));
        stateListAnimator.addState(new int[0], scale(view, 1.0f, 240L));
        view.setStateListAnimator(stateListAnimator);
    }

    static GradientDrawable rounded(Context context, int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(context, i2));
        return gradientDrawable;
    }

    private static Animator scale(View view, float f, long j) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, (Property<View, Float>) View.SCALE_X, f), ObjectAnimator.ofFloat(view, (Property<View, Float>) View.SCALE_Y, f));
        animatorSet.setDuration(j);
        animatorSet.setInterpolator(EASE);
        return animatorSet;
    }

    static Drawable surface(Context context, boolean z) {
        return new GlassSurface(dp(context, 28.0f), dp(context, 1.0f), z);
    }
}
