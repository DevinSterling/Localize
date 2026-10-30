package com.devinsterling.localize.internal;

public final class ExceptionHelper {

    private ExceptionHelper() {}

    public static RuntimeException aggregate(RuntimeException aggregate, RuntimeException exception) {
        if (aggregate == null) {
            return exception;
        } else if (aggregate != exception) {
            aggregate.addSuppressed(exception);
        }
        return aggregate;
    }

    public static RuntimeException aggregateException(RuntimeException aggregate, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            aggregate = aggregate(aggregate, e);
        }

        return aggregate;
    }

    public static void runAllAggregating(Runnable... tasks) {
        RuntimeException aggregate = null;

        for (Runnable task : tasks) {
            aggregate = aggregateException(aggregate, task);
        }

        if (aggregate != null) {
            throw aggregate;
        }
    }
}
