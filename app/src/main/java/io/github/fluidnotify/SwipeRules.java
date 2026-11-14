package io.github.fluidnotify;

/* loaded from: classes.dex */
public final class SwipeRules {
    private SwipeRules() {
    }

    public static boolean canStart(float f, float f2, int i) {
        float edgePercent = (edgePercent(i) * f2) / 100.0f;
        return f2 > 0.0f && edgePercent > 0.0f && f >= 0.0f && f <= f2 && (f <= edgePercent || f >= f2 - edgePercent);
    }

    public static int dismissPercent(int i) {
        return Math.max(20, Math.min(60, i));
    }

    public static int edgePercent(int i) {
        return Math.max(0, Math.min(50, i));
    }

    public static boolean horizontal(float f, float f2) {
        return Math.abs(f) > Math.abs(f2) * 1.5f;
    }

    public static float limit(float f, float f2) {
        float max = Math.max(0.0f, f2) * 0.6f;
        return Math.max(-max, Math.min(max, f));
    }

    public static boolean shouldDismiss(float f, float f2, int i) {
        return f2 > 0.0f && Math.abs(limit(f, f2)) >= threshold(f2, i);
    }

    public static float threshold(float f, int i) {
        return Math.max(0.0f, f) * (dismissPercent(i) / 100.0f);
    }
}
