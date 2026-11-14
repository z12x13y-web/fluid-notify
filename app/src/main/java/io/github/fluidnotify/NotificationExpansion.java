package io.github.fluidnotify;

import android.service.notification.StatusBarNotification;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/* loaded from: classes.dex */
final class NotificationExpansion {
    NotificationExpansion() {
    }

    static void install(ClassLoader classLoader, PluginProfile pluginProfile) {
        XposedBridge.hookAllConstructors(XposedHelpers.findClass(pluginProfile.cardDataModel, classLoader), new XC_MethodHook() { // from class: io.github.fluidnotify.NotificationExpansion.1
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                int normalize;
                try {
                    StatusBarNotification cardCreatorSource = CapsuleTicker.cardCreatorSource(methodHookParam.args[8]);
                    if (cardCreatorSource != null && (normalize = AutoExpandConfig.normalize(cardCreatorSource.getNotification().extras.getInt("io.github.fluidnotify.ticker.expandSeconds", 0))) > 0) {
                        methodHookParam.args[4] = Long.valueOf(normalize * 1000);
                    }
                } catch (Throwable th) {
                    XposedBridge.log("FluidNotify: notification expansion timing failed: " + th.getClass().getSimpleName());
                }
            }
        });
    }
}
