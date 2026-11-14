package io.github.fluidnotify;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/* loaded from: classes.dex */
final class SmallWindowLaunchRequest {
    SmallWindowLaunchRequest() {
    }

    static /* synthetic */ void lambda$submit$0(BooleanSupplier booleanSupplier, IntConsumer intConsumer, int i) {
        if (booleanSupplier.getAsBoolean()) {
            return;
        }
        intConsumer.accept(i);
    }

    static /* synthetic */ void lambda$submit$1(final BooleanSupplier booleanSupplier, IntSupplier intSupplier, Executor executor, final IntConsumer intConsumer) {
        if (booleanSupplier.getAsBoolean()) {
            return;
        }
        final int asInt = intSupplier.getAsInt();
        executor.execute(new Runnable() { // from class: io.github.fluidnotify.SmallWindowLaunchRequest$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                SmallWindowLaunchRequest.lambda$submit$0(booleanSupplier, intConsumer, asInt);
            }
        });
    }

    static void submit(Runnable runnable, final IntSupplier intSupplier, Executor executor, final Executor executor2, final BooleanSupplier booleanSupplier, final IntConsumer intConsumer) {
        runnable.run();
        executor.execute(new Runnable() { // from class: io.github.fluidnotify.SmallWindowLaunchRequest$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SmallWindowLaunchRequest.lambda$submit$1(booleanSupplier, intSupplier, executor2, intConsumer);
            }
        });
    }
}
