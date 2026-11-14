package io.github.fluidnotify;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* loaded from: classes.dex */
final class SmallWindowTransition {
    private static ClassLoader systemLoader;
    private static final Set<Object> ANIMATORS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap()));
    private static final Map<StatusBarNotification, Bundle> TARGETS = Collections.synchronizedMap(new WeakHashMap());
    private static final Map<View, SmallWindowLaunchSession> ACTIVE = new WeakHashMap();
    private static final ExecutorService STARTER = Executors.newSingleThreadExecutor(new ThreadFactory() { // from class: io.github.fluidnotify.SmallWindowTransition$$ExternalSyntheticLambda0
        @Override // java.util.concurrent.ThreadFactory
        public final Thread newThread(Runnable runnable) {
            return SmallWindowTransition.lambda$static$0(runnable);
        }
    });

    SmallWindowTransition() {
    }

    static void cancel(View view) {
        SmallWindowLaunchSession remove = ACTIVE.remove(view);
        if (remove != null) {
            remove.abort();
        }
    }

    static void install(ClassLoader classLoader) {
        systemLoader = classLoader;
        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.animation.ActivityTransitionAnimator", classLoader), new XC_MethodHook() { // from class: io.github.fluidnotify.SmallWindowTransition.1
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                if (methodHookParam.hasThrowable()) {
                    return;
                }
                SmallWindowTransition.ANIMATORS.add(methodHookParam.thisObject);
            }
        });
    }

    static /* synthetic */ void lambda$start$1(Runnable[] runnableArr, Handler handler, View view, SmallWindowSurface[] smallWindowSurfaceArr, Runnable[] runnableArr2) {
        Runnable runnable = runnableArr[0];
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        ACTIVE.remove(view);
        SmallWindowSurface smallWindowSurface = smallWindowSurfaceArr[0];
        if (smallWindowSurface != null) {
            smallWindowSurface.close();
        }
        Runnable runnable2 = runnableArr2[0];
        if (runnable2 != null) {
            runnable2.run();
        }
    }

    static /* synthetic */ void lambda$start$2(Runnable runnable, StatusBarNotification statusBarNotification) {
        if (runnable != null) {
            runnable.run();
        }
        notifyClick(statusBarNotification);
    }

    static /* synthetic */ Object lambda$start$3(boolean z, Bundle bundle, Object obj, SmallWindowSurface[] smallWindowSurfaceArr, final SmallWindowLaunchSession smallWindowLaunchSession, Handler handler, Object obj2, Object obj3, Method method, Object[] objArr) throws Throwable {
        String name = method.getName();
        if (method.getDeclaringClass() == Object.class) {
            if ("hashCode".equals(name)) {
                return Integer.valueOf(System.identityHashCode(obj3));
            }
            if ("equals".equals(name)) {
                return Boolean.valueOf(obj3 == objArr[0]);
            }
            return "FluidNotify window controller";
        }
        if (!"onTransitionAnimationEnd".equals(name) && !"onTransitionAnimationCancelled".equals(name)) {
            r3 = false;
        }
        if (z) {
            if ("isFlingToZoomAnimator".equals(name)) {
                return Boolean.TRUE;
            }
            if ("getZoomBundle".equals(name)) {
                return bundle;
            }
            if ("getCouiInterpolators".equals(name)) {
                return obj;
            }
            if ("onTransitionAnimationStart".equals(name)) {
                smallWindowSurfaceArr[0].start();
                return null;
            }
            if ("onTransitionAnimationProgress".equals(name)) {
                try {
                    smallWindowSurfaceArr[0].update(objArr[0]);
                } catch (Throwable unused) {
                    smallWindowLaunchSession.finish();
                }
                return null;
            }
            if (r3) {
                Objects.requireNonNull(smallWindowLaunchSession);
                handler.post(new Runnable() { // from class: io.github.fluidnotify.SmallWindowTransition$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        SmallWindowLaunchSession.this.finish();
                    }
                });
                return null;
            }
        }
        try {
            Object invoke = method.invoke(obj2, objArr);
            if (r3 || ("onIntentStarted".equals(name) && objArr != null && Boolean.FALSE.equals(objArr[0]))) {
                Objects.requireNonNull(smallWindowLaunchSession);
                handler.post(new Runnable() { // from class: io.github.fluidnotify.SmallWindowTransition$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        SmallWindowLaunchSession.this.finish();
                    }
                });
            }
            return invoke;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    static /* synthetic */ void lambda$start$4(Object obj) {
        try {
            if (XposedHelpers.callMethod(obj, "getDelegate", new Object[0]) != null) {
                XposedHelpers.callMethod(obj, "onAnimationCancelled", new Object[0]);
            }
        } catch (Throwable unused) {
        }
    }

    static /* synthetic */ void lambda$start$5(Object obj, Object obj2, Context context, Object obj3) {
        XposedHelpers.callMethod(obj, "onIntentStarted", new Object[]{true});
        XposedHelpers.callMethod(obj2, "onAnimationStartForZoomWithOutWindowReadyRunner", new Object[]{context, obj3, obj});
    }

    static /* synthetic */ int lambda$start$6(Context context, StatusBarNotification statusBarNotification, ActivityOptions activityOptions) {
        try {
            return send(context, statusBarNotification, activityOptions, true);
        } catch (Throwable th) {
            XposedBridge.log("FluidNotify: asynchronous zoom launch: " + th.getClass().getSimpleName());
            return -1;
        }
    }

    static /* synthetic */ void lambda$start$8(SmallWindowLaunchSession smallWindowLaunchSession, int i) {
        smallWindowLaunchSession.result(i);
        if (smallWindowLaunchSession.isClosed() || i == 0 || i == 2) {
            return;
        }
        smallWindowLaunchSession.finish();
    }

    static /* synthetic */ Object lambda$start$9(View view, Context context, StatusBarNotification statusBarNotification, SmallWindowLaunchSession smallWindowLaunchSession, Object obj, Method method, Object[] objArr) throws Throwable {
        if (method.getDeclaringClass() != Object.class) {
            Object obj2 = objArr[0];
            int send = send(context, statusBarNotification, obj2 == null ? ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight()) : (ActivityOptions) XposedHelpers.callStaticMethod(ActivityOptions.class, "makeRemoteAnimation", new Object[]{obj2}), obj2 != null);
            smallWindowLaunchSession.result(send);
            return Integer.valueOf(send);
        }
        if ("hashCode".equals(method.getName())) {
            return Integer.valueOf(System.identityHashCode(obj));
        }
        if ("equals".equals(method.getName())) {
            return Boolean.valueOf(obj == objArr[0]);
        }
        return "FluidNotify window starter";
    }

    static /* synthetic */ Thread lambda$static$0(Runnable runnable) {
        return new Thread(runnable, "FluidNotify-window-start");
    }

    private static void notifyClick(StatusBarNotification statusBarNotification) {
        try {
            XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.oplus.systemui.notification.utlils.OplusNotificationUtil", systemLoader), "notifyClickToNms", new Object[]{statusBarNotification.getKey()});
        } catch (Throwable unused) {
        }
    }

    static void prepare(Context context, StatusBarNotification statusBarNotification) {
        if (statusBarNotification != null) {
            Map<StatusBarNotification, Bundle> map = TARGETS;
            if (map.containsKey(statusBarNotification) || systemLoader == null) {
                return;
            }
            try {
                Bundle bundle = (Bundle) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.util.HeadsUpToZoomUtils", systemLoader), "calculateFlexibleWindowBounds", new Object[]{new Intent((Intent) XposedHelpers.callMethod(statusBarNotification.getNotification().contentIntent, "getIntent", new Object[0])), context, Integer.valueOf(statusBarNotification.getUserId())});
                if (bundle == null || bundle.getInt("androidx.flexible.ResizeMode", 0) != 4 || bundle.getParcelable("androidx.flexible.LaunchBounds") == null) {
                    return;
                }
                map.put(statusBarNotification, bundle);
            } catch (Throwable th) {
                XposedBridge.log("FluidNotify: zoom geometry unavailable: " + th.getClass().getSimpleName());
            }
        }
    }

    private static int send(Context context, StatusBarNotification statusBarNotification, ActivityOptions activityOptions, boolean z) {
        Bundle bundle = new Bundle();
        bundle.putInt("extra_window_mode", 100);
        bundle.putInt("android:activity.mZoomLaunchFlags", 5);
        bundle.putInt("androidx.activity.LaunchScenario", 1);
        bundle.putString("launch_source_type", "android".equals(statusBarNotification.getOpPkg()) ? "opushnotification" : "localnotification");
        if (z) {
            bundle.putString("notificationStartFrom", "startWithTransitionAnim");
        }
        try {
            XposedHelpers.callMethod(activityOptions, "setExtraBundle", new Object[]{bundle});
        } catch (Throwable unused) {
        }
        Bundle bundle2 = activityOptions.toBundle();
        bundle2.putAll(bundle);
        Intent intent = new Intent((Intent) XposedHelpers.callMethod(statusBarNotification.getNotification().contentIntent, "getIntent", new Object[0]));
        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.oplus.content.OplusIntent", systemLoader), "setOplusFlags", new Object[]{intent, 2048});
        int intValue = ((Number) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.oplus.zoomwindow.OplusZoomWindowManager", systemLoader), "getInstance", new Object[0]), "startZoomWindow", new Object[]{intent, bundle2, Integer.valueOf(statusBarNotification.getUserId()), "com.android.systemui"})).intValue();
        XposedBridge.log("FluidNotify: small-window transition remote=" + z + " result=" + intValue);
        return intValue;
    }

    private static Object springInterpolators(float f) {
        Class findClass = XposedHelpers.findClass("com.coui.appcompat.animation.COUISpringInterpolator", systemLoader);
        double max = Math.max(0.0f, Math.min(12000.0f, f));
        Double valueOf = Double.valueOf(1.0d);
        Object newInstance = XposedHelpers.newInstance(findClass, new Object[]{Double.valueOf(157.91d), Double.valueOf(max > 6000.0d ? 0.8d : 1.0d), Double.valueOf(max), Float.valueOf(1.0f), Float.valueOf(6666.0f)});
        return XposedHelpers.newInstance(XposedHelpers.findClass("com.android.systemui.animation.TransitionAnimator$Interpolators", systemLoader), new Object[]{newInstance, newInstance, XposedHelpers.newInstance(findClass, new Object[]{Double.valueOf(438.65d), valueOf, Double.valueOf(0.0d), Float.valueOf(1.0f), Float.valueOf(6666.0f)}), XposedHelpers.newInstance(findClass, new Object[]{Double.valueOf(438.65d), valueOf, Double.valueOf(0.0d), Float.valueOf(1.0f), Float.valueOf(6666.0f)})});
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0102  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x012c A[Catch: all -> 0x0229, TryCatch #3 {all -> 0x0229, blocks: (B:36:0x0121, B:39:0x012c, B:41:0x014d), top: B:35:0x0121 }] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x01bf A[Catch: all -> 0x0227, TryCatch #2 {all -> 0x0227, blocks: (B:46:0x01b7, B:47:0x01be, B:48:0x01bf, B:50:0x0214, B:51:0x0217, B:53:0x021d, B:54:0x0222), top: B:37:0x012a }] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0104  */
    /* JADX WARN: Type inference failed for: r7v10 */
    /* JADX WARN: Type inference failed for: r7v2 */
    /* JADX WARN: Type inference failed for: r7v8 */
    /* JADX WARN: Type inference failed for: r7v9, types: [java.lang.Class[]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static boolean start(final android.content.Context r26, final android.service.notification.StatusBarNotification r27, final android.view.View r28, float r29, final java.lang.Runnable r30, java.lang.Runnable r31) throws java.lang.Throwable {
        /*
            Method dump skipped, instructions count: 581
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.github.fluidnotify.SmallWindowTransition.start(android.content.Context, android.service.notification.StatusBarNotification, android.view.View, float, java.lang.Runnable, java.lang.Runnable):boolean");
    }
}
