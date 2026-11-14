package io.github.fluidnotify;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;

/* loaded from: classes.dex */
public final class ConfigProvider extends ContentProvider {
    static final String URI = "content://io.github.fluidnotify.config";

    static boolean allows(Context context, int i) {
        if (i < 0) {
            return false;
        }
        boolean z = i == Process.myUid();
        String[] packagesForUid = context.getPackageManager().getPackagesForUid(i);
        if (packagesForUid != null) {
            for (String str : packagesForUid) {
                z |= "com.android.systemui".equals(str);
            }
        }
        return z;
    }

    static Bundle snapshot(Context context) {
        SharedPreferences open = ConfigStore.open(context);
        if (open == null) {
            return null;
        }
        Bundle bundle = AppSelectionSettings.bundle(AppSelectionSettings.read(open));
        bundle.putBoolean("enabled", open.getBoolean("enabled", true));
        bundle.putBoolean("pull_down_small_window", open.getBoolean("pull_down_small_window", false));
        bundle.putInt("swipe_active_edge", SwipeRules.edgePercent(open.getInt("swipe_active_edge", 15)));
        bundle.putInt("swipe_dismiss_distance", SwipeRules.dismissPercent(open.getInt("swipe_dismiss_distance", 50)));
        bundle.putInt("notification_shrink_seconds", DurationConfig.normalize(open.getInt("notification_shrink_seconds", 0)));
        bundle.putInt("notification_auto_expand_seconds", AutoExpandConfig.normalize(open.getInt("notification_auto_expand_seconds", 0)));
        return bundle;
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        if (!allows(getContext(), Binder.getCallingUid())) {
            throw new SecurityException("Configuration is only available to SystemUI");
        }
        if ("get_config".equals(str)) {
            return snapshot(getContext());
        }
        throw new IllegalArgumentException("Unknown method");
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}
