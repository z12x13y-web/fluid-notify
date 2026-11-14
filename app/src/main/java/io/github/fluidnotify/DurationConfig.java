package io.github.fluidnotify;

/* loaded from: classes.dex */
final class DurationConfig {
    static final String KEY = "notification_shrink_seconds";
    static final int MAX = 999;
    private static volatile int seconds;

    DurationConfig() {
    }

    static int normalize(int i) {
        return Math.max(0, Math.min(MAX, i));
    }

    static int seconds() {
        return seconds;
    }

    static void set(int i) {
        seconds = normalize(i);
    }
}
