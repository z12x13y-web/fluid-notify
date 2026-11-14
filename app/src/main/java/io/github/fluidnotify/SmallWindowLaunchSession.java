package io.github.fluidnotify;

/* loaded from: classes.dex */
final class SmallWindowLaunchSession {
    static final long WATCHDOG_MS = 5000;
    private boolean aborted;
    private final Runnable cleanup;
    private volatile boolean closed;
    private final Runnable failed;
    private final Runnable finished;
    private boolean hasResult;
    private int result;
    private boolean terminal;

    SmallWindowLaunchSession(Runnable runnable, Runnable runnable2) {
        this(runnable, runnable2, new Runnable() { // from class: io.github.fluidnotify.SmallWindowLaunchSession$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SmallWindowLaunchSession.lambda$new$0();
            }
        });
    }

    SmallWindowLaunchSession(Runnable runnable, Runnable runnable2, Runnable runnable3) {
        this.result = -1;
        this.cleanup = runnable;
        this.finished = runnable2;
        this.failed = runnable3;
    }

    private void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            this.cleanup.run();
        } finally {
            if (!this.aborted) {
                (succeeded() ? this.finished : this.failed).run();
            }
        }
    }

    private void drain() {
        if (this.hasResult && this.terminal) {
            close();
        }
    }

    static /* synthetic */ void lambda$new$0() {
    }

    void abort() {
        this.aborted = true;
        close();
    }

    void finish() {
        this.terminal = true;
        drain();
    }

    boolean isClosed() {
        return this.closed;
    }

    void result(int i) {
        if (this.hasResult) {
            return;
        }
        this.hasResult = true;
        this.result = i;
        if (i < 0) {
            this.terminal = true;
        }
        drain();
    }

    boolean succeeded() {
        return this.hasResult && this.result >= 0;
    }

    void timeout() {
        if (this.hasResult) {
            finish();
        } else {
            result(-1);
        }
    }
}
