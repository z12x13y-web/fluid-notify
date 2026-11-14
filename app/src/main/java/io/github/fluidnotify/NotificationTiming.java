package io.github.fluidnotify;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/* loaded from: classes.dex */
final class NotificationTiming {
    private static final Map<Object, PluginProfile> STRATEGIES = new WeakHashMap();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile boolean enabled = true;

    NotificationTiming() {
    }

    static void install(ClassLoader classLoader, final PluginProfile pluginProfile) {
        if (pluginProfile.modern || pluginProfile.compact16) {
            Class findClass = XposedHelpers.findClass(pluginProfile.timingStrategy, classLoader);
            XposedBridge.hookAllConstructors(findClass, new XC_MethodHook() { // from class: io.github.fluidnotify.NotificationTiming.1
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    if (methodHookParam.hasThrowable()) {
                        return;
                    }
                    synchronized (NotificationTiming.STRATEGIES) {
                        NotificationTiming.STRATEGIES.put(methodHookParam.thisObject, PluginProfile.this);
                    }
                }
            });
            XposedBridge.hookAllMethods(findClass, pluginProfile.timingDuration, new XC_MethodHook() { // from class: io.github.fluidnotify.NotificationTiming.2
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    int seconds = DurationConfig.seconds();
                    if (!NotificationTiming.enabled || seconds == 0) {
                        return;
                    }
                    try {
                        if (NotificationTiming.selected(methodHookParam.thisObject, PluginProfile.this) != null) {
                            methodHookParam.setResult(Long.valueOf(seconds * 1000));
                        }
                    } catch (Throwable th) {
                        NotificationTiming.report(th);
                    }
                }
            });
            XC_MethodHook xC_MethodHook = new XC_MethodHook() { // from class: io.github.fluidnotify.NotificationTiming.3
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    try {
                        StatusBarNotification selected = NotificationTiming.selected(methodHookParam.thisObject, PluginProfile.this);
                        if (selected == null || !"io.github.fluidnotify".equals(selected.getPackageName())) {
                            return;
                        }
                        XposedBridge.log("FluidNotify: test native shrink timer; id=" + selected.getId() + " durationMs=" + XposedHelpers.getLongField(methodHookParam.thisObject, "e"));
                    } catch (Throwable th) {
                        NotificationTiming.report(th);
                    }
                }

                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    try {
                        if (NotificationTiming.selected(methodHookParam.thisObject, PluginProfile.this) == null) {
                            return;
                        }
                        XposedHelpers.setLongField(methodHookParam.thisObject, "e", ((Long) XposedHelpers.callMethod(methodHookParam.thisObject, PluginProfile.this.timingDuration, new Object[0])).longValue());
                    } catch (Throwable th) {
                        NotificationTiming.report(th);
                    }
                }
            };
            XposedBridge.hookAllMethods(findClass, pluginProfile.timingRegister, xC_MethodHook);
            if (pluginProfile.compact16) {
                XposedBridge.hookAllMethods(findClass, "l", xC_MethodHook);
            }
            XposedBridge.log("FluidNotify: native notification timing hooks installed");
        }
    }

    static /* synthetic */ void lambda$refresh$0() {
        LinkedHashMap linkedHashMap;
        Map<Object, PluginProfile> map = STRATEGIES;
        synchronized (map) {
            linkedHashMap = new LinkedHashMap(map);
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            try {
                XposedHelpers.callMethod(entry.getKey(), ((PluginProfile) entry.getValue()).timingRefresh, new Object[0]);
            } catch (Throwable th) {
                report(th);
            }
        }
    }

    static void refresh() {
        MAIN.post(new Runnable() { // from class: io.github.fluidnotify.NotificationTiming$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                NotificationTiming.lambda$refresh$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void report(Throwable th) {
        XposedBridge.log("FluidNotify: native notification timing failed: " + th.getClass().getSimpleName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static StatusBarNotification selected(Object obj, PluginProfile pluginProfile) {
        Object objectField = XposedHelpers.getObjectField(XposedHelpers.getObjectField(obj, "b"), "a");
        Object objectField2 = XposedHelpers.getObjectField(objectField, pluginProfile.selectedKey);
        for (Object obj2 : (List) XposedHelpers.getObjectField(objectField, pluginProfile.capsuleList)) {
            if (objectField2 != null && objectField2.equals(XposedHelpers.getObjectField(obj2, "a"))) {
                return CapsuleTicker.capsuleSource(obj2);
            }
        }
        if (!pluginProfile.compact16) {
            return null;
        }
        for (Object obj3 : (List) XposedHelpers.getObjectField(objectField, "h")) {
            if (objectField2 != null && objectField2.equals(XposedHelpers.getObjectField(obj3, "a"))) {
                return CapsuleTicker.capsuleSource(obj3);
            }
        }
        return null;
    }

    static void setEnabled(boolean z) {
        enabled = z;
    }
}
