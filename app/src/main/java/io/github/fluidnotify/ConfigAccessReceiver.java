package io.github.fluidnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/* loaded from: classes.dex */
public final class ConfigAccessReceiver extends BroadcastReceiver {
    static final String ACTION = "io.github.fluidnotify.REQUEST_CONFIG_ACCESS";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            context.grantUriPermission("com.android.systemui", Uri.parse("content://io.github.fluidnotify.config"), 1);
        }
    }
}
