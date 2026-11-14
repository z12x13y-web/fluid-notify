package io.github.fluidnotify;

/* loaded from: classes.dex */
final class SwipeConfig {
    static final int DEFAULT = 15;
    static final int DEFAULT_DISTANCE = 50;
    static final String DISTANCE_KEY = "swipe_dismiss_distance";
    static final String KEY = "swipe_active_edge";
    private static volatile int boundary = 15;
    private static volatile int distance = 50;

    SwipeConfig() {
    }

    static int distance() {
        return distance;
    }

    static int percent() {
        return boundary;
    }

    static void set(int i) {
        boundary = SwipeRules.edgePercent(i);
    }

    static void setDistance(int i) {
        distance = SwipeRules.dismissPercent(i);
    }
}
