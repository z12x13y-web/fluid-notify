package io.github.fluidnotify;

/* loaded from: classes.dex */
public final class NotificationPolicy {
    private static final int PERSISTENT_FLAGS = 610;

    private NotificationPolicy() {
    }

    public static boolean shouldConvert(int i, boolean z) {
        return z && (i & PERSISTENT_FLAGS) == 0;
    }
}
