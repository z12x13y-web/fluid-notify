package io.github.fluidnotify;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.github.fluidnotify.PullDownWindow;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/* loaded from: classes.dex */
final class PullDownWindow {
    private static final String BINDING = "io.github.fluidnotify.pullDownBinding";
    private static final String CLAIM = "io.github.fluidnotify.pullDownTouchClaim";
    private static final String STATE = "io.github.fluidnotify.pullDownHandle";
    private static final Set<ClassLoader> READY = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap()));
    private static final Set<FrameLayout> HOSTS = Collections.newSetFromMap(new WeakHashMap());

    private static final class Binding {
        final ClassLoader loader;
        final PluginProfile profile;

        Binding(ClassLoader classLoader, PluginProfile pluginProfile) {
            this.loader = classLoader;
            this.profile = pluginProfile;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static final class Handle extends View {
        ViewGroup cardContainer;
        float closingAlpha;
        View closingCard;
        final float density;
        View draggedCard;
        int generation;
        PullDownGesture gesture;
        final FrameLayout host;
        boolean launching;
        final ClassLoader loader;
        final ArrayList<Neighbour> neighbours;
        float originalPivotX;
        float originalPivotY;
        float originalScaleX;
        float originalScaleY;
        float originalY;
        float originalZ;
        final Paint paint;
        Object pausedItem;
        Object pausedModel;
        final PluginProfile profile;
        float progress;
        float releaseVelocity;
        ValueAnimator settling;
        StatusBarNotification source;
        float startX;
        float startY;
        VelocityTracker velocityTracker;

        Handle(FrameLayout frameLayout, ClassLoader classLoader, PluginProfile pluginProfile) {
            super(frameLayout.getContext());
            this.paint = new Paint(1);
            this.neighbours = new ArrayList<>();
            this.host = frameLayout;
            this.loader = classLoader;
            this.profile = pluginProfile;
            this.density = getResources().getDisplayMetrics().density;
            setContentDescription("下拉以小窗打开应用");
            setFocusable(true);
            setImportantForAccessibility(1);
        }

        private void collapse(View view) {
            if (view == null) {
                return;
            }
            try {
                Object objectField = XposedHelpers.getObjectField(view, this.profile.modern ? "b" : "m");
                Object staticObjectField = XposedHelpers.getStaticObjectField(XposedHelpers.findClass(this.profile.slideAction, this.loader), this.profile.slideSingleton);
                XposedHelpers.callMethod(view, "setOverScrollEnable", new Object[]{false});
                XposedHelpers.callMethod(objectField, this.profile.modern ? "f" : "g", new Object[]{staticObjectField});
            } catch (Throwable th) {
                XposedBridge.log("FluidNotify: small-window card collapse: " + th.getClass().getSimpleName());
            }
        }

        private void drag(float f, float f2) {
            View view = this.draggedCard;
            if (view == null) {
                return;
            }
            view.setTranslationY(this.originalY + (dp(1) * Math.min(40.0f, f2 * 0.32f)));
            this.draggedCard.setScaleX(this.originalScaleX * (1.0f - (0.04f * f)));
            this.draggedCard.setScaleY(this.originalScaleY * ((0.08f * f) + 1.0f));
            this.draggedCard.setTranslationZ(this.originalZ + (dp(12) * f));
            Iterator<Neighbour> it = this.neighbours.iterator();
            while (it.hasNext()) {
                Neighbour next = it.next();
                next.view.setAlpha(next.alpha * (1.0f - (0.8f * f)));
            }
            invalidate();
        }

        private void endTouch() {
            PullDownGesture pullDownGesture = this.gesture;
            if (pullDownGesture != null) {
                pullDownGesture.cancel();
            }
            this.gesture = null;
            VelocityTracker velocityTracker = this.velocityTracker;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                this.velocityTracker = null;
            }
            if (this.pausedModel != null && !this.launching) {
                try {
                    nativeTouch(false);
                } catch (Throwable th) {
                    reportTouch(th);
                }
                this.pausedModel = null;
                this.pausedItem = null;
            }
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        private View findContainer() {
            for (ViewParent viewParent = this.host.getParent(); viewParent != 0; viewParent = viewParent.getParent()) {
                if (viewParent.getClass().getName().equals("com.oplus.systemui.plugins.seedling.card.ui.view.CardContainer")) {
                    return (View) viewParent;
                }
            }
            return this.cardContainer;
        }

        private void nativeTouch(boolean z) {
            XposedHelpers.callMethod(this.pausedModel, this.profile.modern ? "g" : "h", new Object[]{XposedHelpers.newInstance(XposedHelpers.findClass(this.profile.cardItem.substring(0, this.profile.cardItem.lastIndexOf(46) + 1) + (z ? "a" : "b"), this.loader), new Object[]{this.pausedItem})});
        }

        private boolean openWindow() {
            if (this.launching || !isAttachedToWindow()) {
                return false;
            }
            this.launching = true;
            final int i = this.generation + 1;
            this.generation = i;
            final View findContainer = findContainer();
            Iterator<Neighbour> it = this.neighbours.iterator();
            while (it.hasNext()) {
                it.next().view.setAlpha(0.0f);
            }
            Context context = getContext();
            StatusBarNotification statusBarNotification = this.source;
            View view = this.draggedCard;
            if (view == null) {
                view = this.host;
            }
            if (SmallWindowLauncher.open(context, statusBarNotification, view, this.releaseVelocity, new Runnable() { // from class: io.github.fluidnotify.PullDownWindow$Handle$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    PullDownWindow.Handle.this.m77lambda$openWindow$1$iogithubfluidnotifyPullDownWindow$Handle(i, findContainer);
                }
            }, new Runnable() { // from class: io.github.fluidnotify.PullDownWindow$Handle$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    PullDownWindow.Handle.this.m78lambda$openWindow$2$iogithubfluidnotifyPullDownWindow$Handle(i);
                }
            })) {
                return true;
            }
            this.launching = false;
            return false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void pauseTimer(View view) {
            View view2 = this.draggedCard;
            if (view2 == null) {
                return;
            }
            try {
                this.pausedItem = XposedHelpers.getObjectField(view2, this.profile.modern ? "h" : "r");
                this.pausedModel = XposedHelpers.getObjectField(view, this.profile.modern ? "b" : "m");
                nativeTouch(true);
            } catch (Throwable th) {
                this.pausedModel = null;
                this.pausedItem = null;
                reportTouch(th);
            }
        }

        private void reportTouch(Throwable th) {
            XposedBridge.log("FluidNotify: pull-down timer: " + th.getClass().getSimpleName());
        }

        private void restoreCard() {
            View view = this.closingCard;
            if (view != null) {
                view.setAlpha(this.closingAlpha);
                this.closingCard = null;
            }
            Iterator<Neighbour> it = this.neighbours.iterator();
            while (it.hasNext()) {
                Neighbour next = it.next();
                next.view.setAlpha(next.alpha);
            }
            this.neighbours.clear();
            this.cardContainer = null;
            View view2 = this.draggedCard;
            if (view2 == null) {
                return;
            }
            view2.setTranslationY(this.originalY);
            this.draggedCard.setScaleX(this.originalScaleX);
            this.draggedCard.setScaleY(this.originalScaleY);
            this.draggedCard.setPivotX(this.originalPivotX);
            this.draggedCard.setPivotY(this.originalPivotY);
            this.draggedCard.setTranslationZ(this.originalZ);
            this.draggedCard = null;
        }

        private void springBack() {
            final View view = this.draggedCard;
            if (view == null || this.settling != null) {
                return;
            }
            final float translationY = view.getTranslationY();
            final float scaleX = view.getScaleX();
            final float scaleY = view.getScaleY();
            final float f = this.progress;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.settling = ofFloat;
            ofFloat.setDuration(220L);
            this.settling.setInterpolator(new DecelerateInterpolator(1.5f));
            this.settling.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: io.github.fluidnotify.PullDownWindow$Handle$$ExternalSyntheticLambda3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PullDownWindow.Handle.this.m79lambda$springBack$3$iogithubfluidnotifyPullDownWindow$Handle(view, translationY, scaleX, scaleY, f, valueAnimator);
                }
            });
            this.settling.start();
        }

        int dp(int i) {
            return Math.round(i * this.density);
        }

        /* renamed from: lambda$openWindow$0$io-github-fluidnotify-PullDownWindow$Handle, reason: not valid java name */
        /* synthetic */ void m76lambda$openWindow$0$iogithubfluidnotifyPullDownWindow$Handle(int i) {
            if (i == this.generation) {
                reset();
            }
        }

        /* renamed from: lambda$openWindow$1$io-github-fluidnotify-PullDownWindow$Handle, reason: not valid java name */
        /* synthetic */ void m77lambda$openWindow$1$iogithubfluidnotifyPullDownWindow$Handle(final int i, View view) {
            if (i != this.generation) {
                return;
            }
            View view2 = this.draggedCard;
            if (view2 == null) {
                view2 = this.host;
            }
            this.closingCard = view2;
            this.closingAlpha = view2.getAlpha();
            this.closingCard.setAlpha(0.0f);
            collapse(view);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: io.github.fluidnotify.PullDownWindow$Handle$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    PullDownWindow.Handle.this.m76lambda$openWindow$0$iogithubfluidnotifyPullDownWindow$Handle(i);
                }
            }, 250L);
        }

        /* renamed from: lambda$openWindow$2$io-github-fluidnotify-PullDownWindow$Handle, reason: not valid java name */
        /* synthetic */ void m78lambda$openWindow$2$iogithubfluidnotifyPullDownWindow$Handle(int i) {
            if (i != this.generation) {
                return;
            }
            this.launching = false;
            springBack();
            endTouch();
            Toast.makeText(getContext(), "暂时无法打开小窗，请重试", 0).show();
        }

        /* renamed from: lambda$springBack$3$io-github-fluidnotify-PullDownWindow$Handle, reason: not valid java name */
        /* synthetic */ void m79lambda$springBack$3$iogithubfluidnotifyPullDownWindow$Handle(View view, float f, float f2, float f3, float f4, ValueAnimator valueAnimator) {
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            view.setTranslationY(f + ((this.originalY - f) * floatValue));
            view.setScaleX(f2 + ((this.originalScaleX - f2) * floatValue));
            view.setScaleY(f3 + ((this.originalScaleY - f3) * floatValue));
            float f5 = 1.0f - floatValue;
            view.setTranslationZ(this.originalZ + (dp(12) * f4 * f5));
            Iterator<Neighbour> it = this.neighbours.iterator();
            while (it.hasNext()) {
                Neighbour next = it.next();
                next.view.setAlpha(next.alpha * (1.0f - ((0.8f * f4) * f5)));
            }
            this.progress = f4 * f5;
            invalidate();
            if (floatValue == 1.0f) {
                restoreCard();
            }
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            if (this.launching) {
                endTouch();
            } else {
                reset();
            }
            super.onDetachedFromWindow();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float dp = dp(32) + (dp(6) * this.progress);
            float width = (getWidth() - dp) / 2.0f;
            float dp2 = (dp(3) * this.progress) + ((getHeight() - dp(4)) / 2.0f);
            this.paint.setColor(671088640);
            float f = this.density;
            float f2 = dp + width;
            canvas.drawRoundRect(width - f, dp2 - f, f2 + f, dp(4) + dp2 + this.density, dp(3), dp(3), this.paint);
            this.paint.setColor(-285212673);
            canvas.drawRoundRect(width, dp2, f2, dp2 + dp(4), dp(2), dp(2), this.paint);
        }

        @Override // android.view.View
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, "以小窗打开"));
            accessibilityNodeInfo.setClickable(true);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0) {
                if (this.gesture == null) {
                    return false;
                }
                VelocityTracker velocityTracker = this.velocityTracker;
                if (velocityTracker != null) {
                    velocityTracker.addMovement(motionEvent);
                }
                if (actionMasked == 3 || actionMasked == 5 || motionEvent.getPointerCount() != 1 || !PullDownConfig.enabled()) {
                    this.gesture.cancel();
                    springBack();
                    if (actionMasked == 3) {
                        endTouch();
                    }
                    return true;
                }
                float rawX = (motionEvent.getRawX() - this.startX) / this.density;
                float rawY = (motionEvent.getRawY() - this.startY) / this.density;
                if (actionMasked == 2) {
                    float move = this.gesture.move(rawX, rawY);
                    this.progress = move;
                    if (this.settling == null) {
                        drag(move, Math.max(0.0f, rawY));
                    }
                } else if (actionMasked == 1) {
                    if (this.gesture.release(rawX, rawY)) {
                        VelocityTracker velocityTracker2 = this.velocityTracker;
                        if (velocityTracker2 != null) {
                            velocityTracker2.computeCurrentVelocity(1000, 12000.0f);
                            this.releaseVelocity = Math.max(0.0f, this.velocityTracker.getYVelocity());
                        }
                        if (!performClick()) {
                            springBack();
                        }
                    } else {
                        springBack();
                    }
                    endTouch();
                }
                return true;
            }
            if (this.launching) {
                return true;
            }
            if (!PullDownConfig.enabled()) {
                return false;
            }
            reset();
            SmallWindowTransition.prepare(getContext(), this.source);
            VelocityTracker obtain = VelocityTracker.obtain();
            this.velocityTracker = obtain;
            obtain.addMovement(motionEvent);
            FrameLayout frameLayout = this.host;
            this.draggedCard = frameLayout;
            ViewParent viewParent = frameLayout.getParent();
            while (true) {
                if (!(viewParent instanceof View)) {
                    break;
                }
                if (viewParent.getClass().getName().equals("com.oplus.systemui.plugins.seedling.card.ui.view.CardView")) {
                    this.draggedCard = (View) viewParent;
                    break;
                }
                viewParent = viewParent.getParent();
            }
            this.originalY = this.draggedCard.getTranslationY();
            this.originalScaleX = this.draggedCard.getScaleX();
            this.originalScaleY = this.draggedCard.getScaleY();
            this.originalPivotX = this.draggedCard.getPivotX();
            this.originalPivotY = this.draggedCard.getPivotY();
            this.originalZ = this.draggedCard.getTranslationZ();
            this.draggedCard.setPivotX(r0.getWidth() / 2.0f);
            this.draggedCard.setPivotY(0.0f);
            if (this.draggedCard.getParent() instanceof ViewGroup) {
                this.cardContainer = (ViewGroup) this.draggedCard.getParent();
                for (int i = 0; i < this.cardContainer.getChildCount(); i++) {
                    View childAt = this.cardContainer.getChildAt(i);
                    if (childAt != this.draggedCard) {
                        this.neighbours.add(new Neighbour(childAt));
                    }
                }
            }
            this.startX = motionEvent.getRawX();
            this.startY = motionEvent.getRawY();
            this.gesture = new PullDownGesture(ViewConfiguration.get(getContext()).getScaledTouchSlop() / this.density);
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }

        @Override // android.view.View
        public boolean performClick() {
            super.performClick();
            return openWindow();
        }

        void reset() {
            this.generation++;
            View view = this.draggedCard;
            if (view == null) {
                view = this.host;
            }
            SmallWindowTransition.cancel(view);
            this.launching = false;
            endTouch();
            ValueAnimator valueAnimator = this.settling;
            if (valueAnimator != null) {
                valueAnimator.cancel();
                this.settling = null;
            }
            restoreCard();
            this.progress = 0.0f;
            this.releaseVelocity = 0.0f;
            invalidate();
        }
    }

    private static final class Neighbour {
        final float alpha;
        final View view;

        Neighbour(View view) {
            this.view = view;
            this.alpha = view.getAlpha();
        }
    }

    PullDownWindow() {
    }

    static void bind(FrameLayout frameLayout, StatusBarNotification statusBarNotification, ClassLoader classLoader, PluginProfile pluginProfile) {
        if (statusBarNotification != null) {
            XposedHelpers.setAdditionalInstanceField(frameLayout, BINDING, new Binding(classLoader, pluginProfile));
            HOSTS.add(frameLayout);
        } else {
            XposedHelpers.removeAdditionalInstanceField(frameLayout, BINDING);
            HOSTS.remove(frameLayout);
        }
        Handle handle = (Handle) XposedHelpers.getAdditionalInstanceField(frameLayout, STATE);
        if (!READY.contains(classLoader) || !PullDownConfig.enabled() || statusBarNotification == null || !statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) || !SmallWindowLauncher.supports(statusBarNotification)) {
            if (handle != null) {
                handle.reset();
                frameLayout.removeView(handle);
                XposedHelpers.removeAdditionalInstanceField(frameLayout, STATE);
                return;
            }
            return;
        }
        if (handle == null) {
            handle = new Handle(frameLayout, classLoader, pluginProfile);
            XposedHelpers.setAdditionalInstanceField(frameLayout, STATE, handle);
        }
        if (handle.source != statusBarNotification) {
            handle.reset();
        }
        handle.source = statusBarNotification;
        if (handle.getParent() == null) {
            frameLayout.addView(handle, new FrameLayout.LayoutParams(handle.dp(160), handle.dp(24), 81));
        }
    }

    static void configurationChanged() {
        for (FrameLayout frameLayout : (FrameLayout[]) HOSTS.toArray(new FrameLayout[0])) {
            refresh(frameLayout);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Handle hitHandle(View view, MotionEvent motionEvent) {
        Rect rect = new Rect();
        int[] iArr = new int[2];
        for (FrameLayout frameLayout : (FrameLayout[]) HOSTS.toArray(new FrameLayout[0])) {
            Handle handle = (Handle) XposedHelpers.getAdditionalInstanceField(frameLayout, STATE);
            if (handle != null && handle.isShown() && handle.getLocalVisibleRect(rect)) {
                handle.getLocationOnScreen(iArr);
                rect.offset(iArr[0], iArr[1]);
                if (rect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                    for (ViewParent parent = handle.getParent(); parent != null; parent = parent.getParent()) {
                        if (parent == view) {
                            return handle;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    static void install(final ClassLoader classLoader, PluginProfile pluginProfile) {
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.oplus.systemui.plugins.seedling.card.ui.view.CardContainer", classLoader), "dispatchTouchEvent", new Object[]{MotionEvent.class, new XC_MethodHook(100) { // from class: io.github.fluidnotify.PullDownWindow.1
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                View view = (View) methodHookParam.thisObject;
                MotionEvent motionEvent = (MotionEvent) methodHookParam.args[0];
                if (motionEvent == null) {
                    return;
                }
                Handle handle = (Handle) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, PullDownWindow.CLAIM);
                if (motionEvent.getActionMasked() == 0) {
                    handle = PullDownConfig.enabled() ? PullDownWindow.hitHandle(view, motionEvent) : null;
                    XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, PullDownWindow.CLAIM, handle);
                }
                if (handle == null) {
                    return;
                }
                handle.onTouchEvent(motionEvent);
                if (motionEvent.getActionMasked() == 0) {
                    handle.pauseTimer(view);
                }
                if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                    XposedHelpers.removeAdditionalInstanceField(methodHookParam.thisObject, PullDownWindow.CLAIM);
                }
                methodHookParam.setResult(true);
            }
        }});
        XC_MethodHook xC_MethodHook = new XC_MethodHook() { // from class: io.github.fluidnotify.PullDownWindow.2
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                if (PullDownWindow.READY.contains(classLoader) && PullDownConfig.enabled() && (methodHookParam.args[0] instanceof StatusBarNotification)) {
                    StatusBarNotification statusBarNotification = (StatusBarNotification) methodHookParam.args[0];
                    if (!statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) || ReplyActions.find(statusBarNotification) == null) {
                        return;
                    }
                    methodHookParam.args[0] = ReplyActions.forCard(statusBarNotification, SmallWindowLauncher.supports(statusBarNotification));
                }
            }
        };
        for (String str : pluginProfile.cardConverters) {
            int i = 0;
            for (Method method : XposedHelpers.findClass(str, classLoader).getDeclaredMethods()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0 && (parameterTypes[0] == StatusBarNotification.class || (pluginProfile.compact16 && "a".equals(method.getName()) && parameterTypes[0] == Object.class))) {
                    XposedBridge.hookMethod(method, xC_MethodHook);
                    i++;
                }
            }
            if (i == 0) {
                throw new IllegalStateException("No card converter entry");
            }
        }
        READY.add(classLoader);
    }

    static void refresh(FrameLayout frameLayout) {
        Binding binding = (Binding) XposedHelpers.getAdditionalInstanceField(frameLayout, BINDING);
        if (binding != null) {
            bind(frameLayout, (StatusBarNotification) XposedHelpers.getAdditionalInstanceField(frameLayout, "io.github.fluidnotify.cardSource"), binding.loader, binding.profile);
        }
    }
}
