package io.github.fluidnotify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.github.fluidnotify.CardSwipe;

/* loaded from: classes.dex */
final class CardSwipe {
    private static final String CARD = "com.oplus.systemui.plugins.seedling.card.ui.view.CardView";
    private static final String STATE = "io.github.fluidnotify.swipeState";

    /* JADX INFO: Access modifiers changed from: private */
    static final class Swipe {
        final float alpha;
        ValueAnimator animator;
        final float density;
        int direction;
        boolean dismissing;
        final int distance;
        boolean ended;
        final String itemField;
        final ClassLoader loader;
        boolean nativeCancelled;
        final StatusBarNotification sbn;
        final float slop;
        final View view;
        final float x;
        final float y;

        Swipe(View view, StatusBarNotification statusBarNotification, ClassLoader classLoader, String str, MotionEvent motionEvent) {
            this.view = view;
            this.sbn = statusBarNotification;
            this.loader = classLoader;
            this.itemField = str;
            float rawX = motionEvent.getRawX();
            this.x = rawX;
            this.y = motionEvent.getRawY();
            float f = view.getResources().getDisplayMetrics().density;
            this.density = f;
            this.slop = Math.max(ViewConfiguration.get(view.getContext()).getScaledTouchSlop(), f * 8.0f);
            this.alpha = view.getAlpha();
            int distance = SwipeConfig.distance();
            this.distance = distance;
            view.getLocationOnScreen(new int[2]);
            if (!SwipeRules.canStart(rawX - r6[0], view.getWidth(), SwipeConfig.percent())) {
                this.direction = 3;
            }
            if ("io.github.fluidnotify".equals(statusBarNotification.getPackageName())) {
                XposedBridge.log("FluidNotify: test swipe start; id=" + statusBarNotification.getId() + " edge=" + SwipeConfig.percent() + " distance=" + distance + " allowed=" + (this.direction != 3));
            }
        }

        private void cancelNative(XC_MethodHook.MethodHookParam methodHookParam, MotionEvent motionEvent) throws Throwable {
            MotionEvent obtain = MotionEvent.obtain(motionEvent);
            obtain.setAction(3);
            try {
                XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, new Object[]{this.view, obtain});
                obtain.recycle();
                this.nativeCancelled = true;
            } catch (Throwable th) {
                obtain.recycle();
                throw th;
            }
        }

        void drag(float f) {
            this.view.setTranslationX(f);
            this.view.setAlpha(this.alpha * (1.0f - (Math.min(1.0f, Math.abs(f) / Math.max(1.0f, this.view.getWidth())) * 0.25f)));
        }

        boolean handle(XC_MethodHook.MethodHookParam methodHookParam, MotionEvent motionEvent) throws Throwable {
            boolean z = false;
            if (this.ended) {
                return this.direction == 1;
            }
            int actionMasked = motionEvent.getActionMasked();
            float rawX = motionEvent.getRawX() - this.x;
            float rawY = motionEvent.getRawY() - this.y;
            if (this.direction == 3) {
                if (actionMasked == 2 && Math.max(Math.abs(rawX), Math.abs(rawY)) > this.slop && !this.nativeCancelled) {
                    cancelNative(methodHookParam, motionEvent);
                    if ("io.github.fluidnotify".equals(this.sbn.getPackageName())) {
                        XposedBridge.log("FluidNotify: edge swipe ignored; id=" + this.sbn.getId());
                    }
                }
                if (actionMasked == 1 || actionMasked == 3) {
                    XposedHelpers.removeAdditionalInstanceField(this.view, CardSwipe.STATE);
                }
                return actionMasked == 2 || this.nativeCancelled;
            }
            if (actionMasked == 5 || motionEvent.getPointerCount() > 1) {
                if (this.direction == 1) {
                    this.ended = true;
                    settle(false);
                    return true;
                }
                this.direction = 2;
            }
            if (actionMasked == 2 && this.direction == 0 && Math.max(Math.abs(rawX), Math.abs(rawY)) > this.slop) {
                if (XposedHelpers.getBooleanField(methodHookParam.thisObject, "h") || !SwipeRules.horizontal(rawX, rawY)) {
                    this.direction = 2;
                } else {
                    this.direction = 1;
                    cancelNative(methodHookParam, motionEvent);
                    if (this.view.getParent() != null) {
                        this.view.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
            if (this.direction != 1) {
                if (actionMasked == 1 || actionMasked == 3) {
                    XposedHelpers.removeAdditionalInstanceField(this.view, CardSwipe.STATE);
                }
                return actionMasked == 2 && this.direction == 0;
            }
            if (actionMasked == 2) {
                drag(SwipeRules.limit(rawX, this.view.getWidth()));
            } else if (actionMasked == 1 || actionMasked == 3) {
                this.ended = true;
                drag(SwipeRules.limit(rawX, this.view.getWidth()));
                if (actionMasked == 1 && SwipeRules.shouldDismiss(rawX, this.view.getWidth(), this.distance)) {
                    z = true;
                }
                settle(z);
            }
            return true;
        }

        /* renamed from: lambda$settle$0$io-github-fluidnotify-CardSwipe$Swipe, reason: not valid java name */
        /* synthetic */ void m29lambda$settle$0$iogithubfluidnotifyCardSwipe$Swipe(boolean z, ValueAnimator valueAnimator) {
            drag(((Float) valueAnimator.getAnimatedValue()).floatValue());
            if (z) {
                this.view.setAlpha(this.alpha * (1.0f - valueAnimator.getAnimatedFraction()));
            }
        }

        void settle(final boolean z) {
            this.dismissing = z;
            if (this.view.getParent() != null) {
                this.view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            float translationX = this.view.getTranslationX();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(translationX, z ? Math.copySign(this.view.getResources().getDisplayMetrics().widthPixels + (this.density * 32.0f), translationX) : 0.0f);
            this.animator = ofFloat;
            ofFloat.setDuration(z ? 200L : 220L);
            this.animator.setInterpolator(new DecelerateInterpolator(1.5f));
            this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: io.github.fluidnotify.CardSwipe$Swipe$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CardSwipe.Swipe.this.m29lambda$settle$0$iogithubfluidnotifyCardSwipe$Swipe(z, valueAnimator);
                }
            });
            this.animator.addListener(new AnimatorListenerAdapter() { // from class: io.github.fluidnotify.CardSwipe.Swipe.1
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
                    Object additionalInstanceField = XposedHelpers.getAdditionalInstanceField(Swipe.this.view, CardSwipe.STATE);
                    Swipe swipe = Swipe.this;
                    if (additionalInstanceField != swipe) {
                        return;
                    }
                    if (!z) {
                        CardSwipe.reset(swipe.view);
                        return;
                    }
                    try {
                        StatusBarNotification source = CardSwipe.source(swipe.view, Swipe.this.itemField);
                        if (source != null && Swipe.this.sbn.getKey().equals(source.getKey())) {
                            CapsuleTicker.dismiss(Swipe.this.loader, Swipe.this.sbn);
                            if ("io.github.fluidnotify".equals(Swipe.this.sbn.getPackageName())) {
                                XposedBridge.log("FluidNotify: animated swipe dismissed; id=" + Swipe.this.sbn.getId());
                                return;
                            }
                            return;
                        }
                    } catch (Throwable th) {
                        XposedBridge.log("FluidNotify: card-swipe dismiss: " + th);
                    }
                    CardSwipe.reset(Swipe.this.view);
                }
            });
            this.animator.start();
        }
    }

    CardSwipe() {
    }

    static void install(final ClassLoader classLoader, PluginProfile pluginProfile) {
        final Class findClass = XposedHelpers.findClass(CARD, classLoader);
        final String str = pluginProfile.modern ? "h" : "r";
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(pluginProfile.swipeHelper, classLoader), "b", new Object[]{View.class, MotionEvent.class, new XC_MethodHook() { // from class: io.github.fluidnotify.CardSwipe.1
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                View view = (View) methodHookParam.args[0];
                if (findClass.isInstance(view)) {
                    try {
                        MotionEvent motionEvent = (MotionEvent) methodHookParam.args[1];
                        Swipe swipe = (Swipe) XposedHelpers.getAdditionalInstanceField(view, CardSwipe.STATE);
                        if (motionEvent.getActionMasked() != 0) {
                            if (swipe == null || !swipe.handle(methodHookParam, motionEvent)) {
                                return;
                            }
                            methodHookParam.setResult((Object) null);
                            return;
                        }
                        if (swipe != null && swipe.dismissing) {
                            methodHookParam.setResult((Object) null);
                            return;
                        }
                        CardSwipe.reset(view);
                        StatusBarNotification source = CardSwipe.source(view, str);
                        if (source == null) {
                            return;
                        }
                        XposedHelpers.setAdditionalInstanceField(view, CardSwipe.STATE, new Swipe(view, source, classLoader, str, motionEvent));
                    } catch (Throwable th) {
                        CardSwipe.reset(view);
                        XposedBridge.log("FluidNotify: card-swipe touch: " + th);
                    }
                }
            }
        }});
        XC_MethodHook xC_MethodHook = new XC_MethodHook() { // from class: io.github.fluidnotify.CardSwipe.2
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                CardSwipe.reset((View) methodHookParam.thisObject);
            }
        };
        XposedHelpers.findAndHookMethod(findClass, "h", new Object[]{XposedHelpers.findClass(pluginProfile.cardItem, classLoader), xC_MethodHook});
        XposedHelpers.findAndHookMethod(findClass, pluginProfile.modern ? "j" : "i", new Object[]{xC_MethodHook});
        XposedBridge.log("FluidNotify: card swipe 0.3.6 installed; ColorOS ".concat(pluginProfile.modern ? "17" : "16"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void reset(View view) {
        Swipe swipe = (Swipe) XposedHelpers.removeAdditionalInstanceField(view, STATE);
        if (swipe == null) {
            return;
        }
        if (swipe.animator != null) {
            swipe.animator.cancel();
        }
        if (swipe.direction == 1) {
            view.setTranslationX(0.0f);
            view.setAlpha(swipe.alpha);
            if (view.getParent() != null) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static StatusBarNotification source(View view, String str) {
        StatusBarNotification statusBarNotification;
        Object objectField = XposedHelpers.getObjectField(view, str);
        if (objectField == null) {
            return null;
        }
        Object objectField2 = XposedHelpers.getObjectField(objectField, "c");
        if ((objectField2 instanceof View) && (statusBarNotification = (StatusBarNotification) XposedHelpers.getAdditionalInstanceField(objectField2, "io.github.fluidnotify.cardSource")) != null && statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) && NotificationPolicy.shouldConvert(statusBarNotification.getNotification().flags, true)) {
            return statusBarNotification;
        }
        return null;
    }
}
