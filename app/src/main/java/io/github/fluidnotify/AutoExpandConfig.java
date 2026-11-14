package io.github.fluidnotify;

/* loaded from: classes.dex */
final class AutoExpandConfig {
    static final String EXTRA = "io.github.fluidnotify.ticker.expandSeconds";
    static final String KEY = "notification_auto_expand_seconds";
    static final int MAX = 999;
    private static volatile int seconds;

    AutoExpandConfig() {
    }

    static int normalize(int i) {
        return Math.max(0, Math.min(MAX, i));
    }

    static int remindType() {
        return seconds == 0 ? 0 : 11;
    }

    static int seconds() {
        return seconds;
    }

    static void set(int i) {
        seconds = normalize(i);
    }
}
