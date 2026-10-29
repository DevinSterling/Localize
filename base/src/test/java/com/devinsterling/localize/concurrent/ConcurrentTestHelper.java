package com.devinsterling.localize.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ConcurrentTestHelper {
    public static final int ITERATIONS = 5000;
    public static final long TIMEOUT_SECONDS = 5;

    private static final ExecutorService POOL = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task, "interleave");
        thread.setDaemon(true);
        return thread;
    });

    private ConcurrentTestHelper() {}

    /// Runs the given `task` on another thread.
    public static Task background(Runnable task) {
        return new Task(POOL.submit(() -> {
            task.run();
            return null;
        }));
    }

    /// Starts all tasks simultaneously and waits for all of them.
    ///
    /// The first failure is rethrown with any subsequent appended as suppressed.
    public static void race(Runnable... runners) {
        CyclicBarrier start = new CyclicBarrier(runners.length);
        Task[] tasks = new Task[runners.length];

        for (int i = 0; i < runners.length; i++) {
            Runnable runner = runners[i];
            tasks[i] = background(() -> {
                await(start);
                runner.run();
            });
        }

        RuntimeException exception = null;
        for (Task task : tasks) {
            try {
                task.join();
            } catch (Exception e) {
                if (exception == null) {
                    exception = asRuntimeException(e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private static RuntimeException asRuntimeException(Throwable e) {
        if (e instanceof RuntimeException runtimeException) {
            return runtimeException;
        } else {
            return new IllegalStateException(e);
        }
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(e);
        }
    }

    public record Task(Future<?> future) {
        public void join() {
            try {
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw asRuntimeException(e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (TimeoutException e) {
                throw new AssertionError("Task timed out after " + TIMEOUT_SECONDS + " seconds", e);
            }
        }
    }
}
