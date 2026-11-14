package io.github.fluidnotify;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Toast;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.Objects;

/* loaded from: classes.dex */
final class SmallWindowLauncher {
    private static volatile Object manager;
    private static volatile Object repository;
    private static volatile Class<?> zoom;

    SmallWindowLauncher() {
    }

    static void attach(Object obj, ClassLoader classLoader) {
        repository = obj;
        try {
            zoom = XposedHelpers.findClass("com.oplus.systemui.notification.utlils.NotificationZoomStartUtil", classLoader);
            manager = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.oplus.zoomwindow.OplusZoomWindowManager", classLoader), "getInstance", new Object[0]);
        } catch (Throwable th) {
            zoom = null;
            manager = null;
            report(th);
        }
    }

    static /* synthetic */ void lambda$open$0() {
    }

    static boolean open(Context context, StatusBarNotification statusBarNotification) {
        return open(context, statusBarNotification, null, 0.0f, null);
    }

    static boolean open(Context context, StatusBarNotification statusBarNotification, View view, float f, Runnable runnable) {
        return open(context, statusBarNotification, view, f, runnable, new Runnable() { // from class: io.github.fluidnotify.SmallWindowLauncher$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SmallWindowLauncher.lambda$open$0();
            }
        });
    }

    static boolean open(Context context, StatusBarNotification statusBarNotification, View view, float f, Runnable runnable, Runnable runnable2) {
        if (PullDownConfig.enabled() && statusBarNotification != null) {
            try {
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
                if (keyguardManager != null && !keyguardManager.isKeyguardLocked()) {
                    Object obj = repository;
                    StatusBarNotification statusBarNotification2 = obj == null ? null : (StatusBarNotification) XposedHelpers.callMethod(obj, "getLiveAlertNotification", new Object[]{statusBarNotification.getKey()});
                    if (statusBarNotification2 != null && statusBarNotification2.getPostTime() == statusBarNotification.getPostTime() && statusBarNotification2.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) && Objects.equals(statusBarNotification2.getNotification().contentIntent, statusBarNotification.getNotification().contentIntent)) {
                        if (!supports(statusBarNotification2)) {
                            Toast.makeText(context, "此应用当前不支持小窗打开", 0).show();
                            return false;
                        }
                        if (view != null) {
                            return SmallWindowTransition.start(context, statusBarNotification, view, f, runnable, runnable2);
                        }
                        XposedHelpers.callStaticMethod(zoom, "startZoomFromNotification", new Object[]{new Intent(target(statusBarNotification2)), statusBarNotification2.getKey(), Integer.valueOf(userId(statusBarNotification2)), "com.android.systemui", 21, statusBarNotification2.getOpPkg()});
                        return true;
                    }
                    return false;
                }
                Toast.makeText(context, "请先解锁，再下拉打开小窗", 0).show();
                return false;
            } catch (Throwable th) {
                report(th);
                Toast.makeText(context, "暂时无法打开小窗，请重试", 0).show();
            }
        }
        return false;
    }

    private static void report(Throwable th) {
        XposedBridge.log("FluidNotify: small-window launch: " + th.getClass().getSimpleName());
    }

    static boolean supports(StatusBarNotification statusBarNotification) {
        Intent target;
        if (statusBarNotification == null || zoom == null || manager == null) {
            return false;
        }
        try {
            int userId = userId(statusBarNotification);
            if (userId < 0 || (target = target(statusBarNotification)) == null) {
                return false;
            }
            ComponentName component = target.getComponent();
            String packageName = component != null ? component.getPackageName() : target.getPackage();
            if (packageName == null) {
                packageName = statusBarNotification.getPackageName();
            }
            if (!Boolean.TRUE.equals(XposedHelpers.callMethod(manager, "isSupportZoomMode", new Object[]{packageName, Integer.valueOf(userId), "com.android.systemui", null}))) {
                return false;
            }
            if (component != null) {
                if (!Boolean.TRUE.equals(XposedHelpers.callMethod(manager, "isSupportZoomMode", new Object[]{component.flattenToShortString(), Integer.valueOf(userId), "com.android.systemui", null}))) {
                    return false;
                }
            }
            return true;
        } catch (Throwable unused) {
            return false;
        }
    }

    private static Intent target(StatusBarNotification statusBarNotification) {
        PendingIntent pendingIntent = statusBarNotification.getNotification().contentIntent;
        if (pendingIntent == null || !pendingIntent.isActivity()) {
            return null;
        }
        return (Intent) XposedHelpers.callMethod(pendingIntent, "getIntent", new Object[0]);
    }

    private static int userId(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getUserId();
    }
}
