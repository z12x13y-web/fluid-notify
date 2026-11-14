package io.github.fluidnotify;

import java.util.function.Consumer;
import java.util.function.Supplier;

/* loaded from: classes.dex */
final class ConfigReload<T> {
    private final Consumer<T> apply;
    private long generation;
    private final Queue main;
    private final Supplier<T> read;
    private final Consumer<RuntimeException> report;
    private final Runnable retry = new Runnable() { // from class: io.github.fluidnotify.ConfigReload$$ExternalSyntheticLambda1
        @Override // java.lang.Runnable
        public final void run() {
            ConfigReload.this.load();
        }
    };
    private long retryDelay = 1000;
    private final Consumer<Runnable> worker;

    interface Queue {
        void post(Runnable runnable, long j);

        void remove(Runnable runnable);
    }

    ConfigReload(Queue queue, Consumer<Runnable> consumer, Supplier<T> supplier, Consumer<T> consumer2, Consumer<RuntimeException> consumer3) {
        this.main = queue;
        this.worker = consumer;
        this.read = supplier;
        this.apply = consumer2;
        this.report = consumer3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void load() {
        final long j = this.generation;
        this.worker.accept(new Runnable() { // from class: io.github.fluidnotify.ConfigReload$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                ConfigReload.this.m38lambda$load$1$iogithubfluidnotifyConfigReload(j);
            }
        });
    }

    /* renamed from: lambda$load$0$io-github-fluidnotify-ConfigReload, reason: not valid java name */
    /* synthetic */ void m37lambda$load$0$iogithubfluidnotifyConfigReload(long j, Object obj, RuntimeException runtimeException) {
        if (j != this.generation) {
            return;
        }
        if (obj != null) {
            this.apply.accept(obj);
            return;
        }
        if (runtimeException != null && this.retryDelay == 1000) {
            this.report.accept(runtimeException);
        }
        this.main.post(this.retry, this.retryDelay);
        this.retryDelay = Math.min(30000L, this.retryDelay * 2);
    }

    /* renamed from: lambda$load$1$io-github-fluidnotify-ConfigReload, reason: not valid java name */
    /* synthetic */ void m38lambda$load$1$iogithubfluidnotifyConfigReload(final long j) {
        final T t;
        final RuntimeException runtimeException;
        try {
            runtimeException = null;
            t = this.read.get();
        } catch (RuntimeException e) {
            t = null;
            runtimeException = e;
        }
        this.main.post(new Runnable() { // from class: io.github.fluidnotify.ConfigReload$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                ConfigReload.this.m37lambda$load$0$iogithubfluidnotifyConfigReload(j, t, runtimeException);
            }
        }, 0L);
    }

    void request() {
        this.generation++;
        this.retryDelay = 1000L;
        this.main.remove(this.retry);
        load();
    }
}
