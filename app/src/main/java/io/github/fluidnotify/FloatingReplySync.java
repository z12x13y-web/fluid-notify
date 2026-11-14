package io.github.fluidnotify;

import android.app.Notification;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;

/* loaded from: classes.dex */
final class FloatingReplySync {
    private static final String ROOT = "com.oplus.systemui.statusbar.notification.livealert.data.";
    private static final String SNAPSHOT = "io.github.fluidnotify.publishedFloatingReply";
    private static final Set<StatusBarNotification> SOURCES = Collections.newSetFromMap(new WeakHashMap());
    private static volatile Object repository;

    /* renamed from: io.github.fluidnotify.FloatingReplySync$1, reason: invalid class name */
    class AnonymousClass1 extends XC_MethodHook {
        final /* synthetic */ BooleanSupplier val$enabled;
        final /* synthetic */ ClassLoader val$loader;
        final /* synthetic */ Handler val$main;

        AnonymousClass1(Handler handler, ClassLoader classLoader, BooleanSupplier booleanSupplier) {
            this.val$main = handler;
            this.val$loader = classLoader;
            this.val$enabled = booleanSupplier;
        }

        protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
            if (methodHookParam.hasThrowable() || methodHookParam.args.length != 2 || methodHookParam.args[0] == null) {
                return;
            }
            try {
                final Object obj = methodHookParam.args[0];
                final StatusBarNotification statusBarNotification = (StatusBarNotification) XposedHelpers.callMethod(obj, "getSbn", new Object[0]);
                if (XposedHelpers.getAdditionalInstanceField(statusBarNotification, FloatingReplySync.SNAPSHOT) == null) {
                    return;
                }
                Handler handler = this.val$main;
                final ClassLoader classLoader = this.val$loader;
                final BooleanSupplier booleanSupplier = this.val$enabled;
                handler.post(new Runnable() { // from class: io.github.fluidnotify.FloatingReplySync$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        FloatingReplySync.refresh(classLoader, booleanSupplier, obj, statusBarNotification);
                    }
                });
            } catch (Throwable th) {
                FloatingReplySync.report(th);
            }
        }
    }

    private static final class Snapshot {
        final Notification.Action reply;

        Snapshot(Notification.Action action) {
            this.reply = action;
        }
    }

    FloatingReplySync() {
    }

    static void attach(Object obj) {
        repository = obj;
    }

    static void install(ClassLoader classLoader, BooleanSupplier booleanSupplier) {
        Class findClassIfExists = XposedHelpers.findClassIfExists("com.oplus.systemui.notification.smallreply.SmallWindowReplyController", classLoader);
        if (findClassIfExists == null) {
            return;
        }
        XposedBridge.hookAllMethods(findClassIfExists, "onNotificationPosted", new AnonymousClass1(new Handler(Looper.getMainLooper()), classLoader, booleanSupplier));
    }

    private static void publish(ClassLoader classLoader, Object obj, StatusBarNotification statusBarNotification) throws Throwable {
        StatusBarNotification clone = statusBarNotification.clone();
        JSONObject jSONObject = new JSONObject(clone.getNotification().extras.getString("oplusLiveAlertOptions", "{}"));
        jSONObject.put("isMilestone", false);
        clone.getNotification().extras.putString("oplusLiveAlertOptions", jSONObject.toString());
        XposedHelpers.callMethod(XposedHelpers.getObjectField(obj, "_liveAlerts"), "setValue", new Object[]{XposedHelpers.newInstance(XposedHelpers.findClass("com.oplus.systemui.statusbar.notification.livealert.data.model.LiveAlertAction$PostAction", classLoader), new Object[]{new ArrayList(Collections.singletonList(clone))})});
        record(statusBarNotification);
    }

    static void record(StatusBarNotification statusBarNotification) {
        Set<StatusBarNotification> set = SOURCES;
        synchronized (set) {
            set.add(statusBarNotification);
        }
        XposedHelpers.setAdditionalInstanceField(statusBarNotification, SNAPSHOT, new Snapshot(reply(statusBarNotification)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void refresh(ClassLoader classLoader, BooleanSupplier booleanSupplier, Object obj, StatusBarNotification statusBarNotification) {
        try {
            Object obj2 = repository;
            if (obj2 != null && booleanSupplier.getAsBoolean() && statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) && XposedHelpers.callMethod(obj, "getSbn", new Object[0]) == statusBarNotification && XposedHelpers.callMethod(obj2, "getLiveAlertNotification", new Object[]{statusBarNotification.getKey()}) == statusBarNotification) {
                Snapshot snapshot = (Snapshot) XposedHelpers.getAdditionalInstanceField(statusBarNotification, SNAPSHOT);
                Notification.Action reply = reply(statusBarNotification);
                if (snapshot != null && snapshot.reply != reply) {
                    publish(classLoader, obj2, statusBarNotification);
                }
            }
        } catch (Throwable th) {
            report(th);
        }
    }

    static void refreshAll() {
        StatusBarNotification[] statusBarNotificationArr;
        Object obj = repository;
        if (obj == null) {
            return;
        }
        Set<StatusBarNotification> set = SOURCES;
        synchronized (set) {
            statusBarNotificationArr = (StatusBarNotification[]) set.toArray(new StatusBarNotification[0]);
        }
        for (StatusBarNotification statusBarNotification : statusBarNotificationArr) {
            try {
                if (statusBarNotification.getNotification().extras.getBoolean("io.github.fluidnotify.ticker", false) && XposedHelpers.callMethod(obj, "getLiveAlertNotification", new Object[]{statusBarNotification.getKey()}) == statusBarNotification) {
                    publish(obj.getClass().getClassLoader(), obj, statusBarNotification);
                }
            } catch (Throwable th) {
                report(th);
            }
        }
    }

    private static Notification.Action reply(StatusBarNotification statusBarNotification) {
        return ReplyActions.find(statusBarNotification);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void report(Throwable th) {
        XposedBridge.log("FluidNotify: floating reply synchronization failed: " + th.getClass().getSimpleName());
    }
}
