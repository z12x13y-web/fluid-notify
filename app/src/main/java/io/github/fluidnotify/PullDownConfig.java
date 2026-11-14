package io.github.fluidnotify;

import android.os.Bundle;

/* loaded from: classes.dex */
final class PullDownConfig {
    static final String KEY = "pull_down_small_window";
    private static volatile boolean active;
    private static volatile boolean selected;

    PullDownConfig() {
    }

    static void apply(Bundle bundle, boolean z) {
        boolean z2 = false;
        if (bundle.containsKey(KEY)) {
            selected = bundle.getBoolean(KEY, false);
        }
        if (z && selected) {
            z2 = true;
        }
        active = z2;
    }

    static boolean enabled() {
        return active;
    }
}
