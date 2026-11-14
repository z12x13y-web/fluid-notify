package io.github.fluidnotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;

/* loaded from: classes.dex */
final class ConfigStore {
    private static final String MIGRATED = "legacy_settings_imported";
    private static final String STORE = "boot_settings";

    private ConfigStore() {
    }

    static synchronized SharedPreferences open(Context context) {
        synchronized (ConfigStore.class) {
            SharedPreferences sharedPreferences = context.createDeviceProtectedStorageContext().getSharedPreferences(STORE, 0);
            if (sharedPreferences.getBoolean(MIGRATED, false)) {
                return sharedPreferences;
            }
            if (!((UserManager) context.getSystemService(UserManager.class)).isUserUnlocked()) {
                return null;
            }
            SharedPreferences sharedPreferences2 = context.getSharedPreferences("settings", 0);
            if (sharedPreferences.edit().putBoolean("enabled", sharedPreferences2.getBoolean("enabled", true)).putInt("notification_shrink_seconds", DurationConfig.normalize(sharedPreferences2.getInt("notification_shrink_seconds", 0))).putInt("notification_auto_expand_seconds", AutoExpandConfig.normalize(sharedPreferences2.getInt("notification_auto_expand_seconds", 0))).putInt("swipe_active_edge", SwipeRules.edgePercent(sharedPreferences2.getInt("swipe_active_edge", 15))).putInt("swipe_dismiss_distance", SwipeRules.dismissPercent(sharedPreferences2.getInt("swipe_dismiss_distance", 50))).putBoolean(MIGRATED, true).commit()) {
                return sharedPreferences;
            }
            sharedPreferences.edit().remove(MIGRATED).commit();
            throw new IllegalStateException("Cannot persist migrated configuration");
        }
    }
}
