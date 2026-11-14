package io.github.fluidnotify;

/* loaded from: classes.dex */
final class PullDownGesture {
    static final float DISTANCE = 48.0f;
    private int direction;
    private final float slop;

    PullDownGesture(float f) {
        this.slop = Math.max(8.0f, f);
    }

    void cancel() {
        this.direction = 2;
    }

    float move(float f, float f2) {
        if (this.direction == 0 && Math.max(Math.abs(f), Math.abs(f2)) > this.slop) {
            this.direction = f2 > Math.abs(f) * 1.25f ? 1 : 2;
        }
        if (this.direction == 1) {
            return Math.max(0.0f, Math.min(1.0f, f2 / DISTANCE));
        }
        return 0.0f;
    }

    boolean release(float f, float f2) {
        move(f, f2);
        boolean z = this.direction == 1 && f2 >= DISTANCE && f2 > Math.abs(f) * 1.25f;
        cancel();
        return z;
    }
}
