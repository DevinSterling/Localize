package com.devinsterling.localize.internal;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class AggregateTest {
    @Test void aggregateExceptions() {
        RuntimeException source = new RuntimeException();
        RuntimeException b = new RuntimeException();
        RuntimeException c = new RuntimeException();

        RuntimeException aggregate = ExceptionHelper.aggregate(null, source);

        assertSame(aggregate, source);
        assertSame(source, ExceptionHelper.aggregate(aggregate, b));
        assertSame(source, ExceptionHelper.aggregate(aggregate, c));
        assertArrayEquals(new Throwable[] { b, c }, source.getSuppressed());
    }

    @Test void aggregateExceptionCannotSuppressItself() {
        RuntimeException source = new RuntimeException();
        RuntimeException aggregate = ExceptionHelper.aggregate(source, source);

        assertSame(aggregate, source);

        aggregate = ExceptionHelper.aggregate(aggregate, source);

        assertSame(aggregate, source);
        assertEquals(0, aggregate.getSuppressed().length);
    }

    @Test void allTasksRunOnException() {
        AtomicInteger count = new AtomicInteger();
        AtomicInteger exceptionCount = new AtomicInteger();
        Runnable incrementTask = count::incrementAndGet;
        Runnable exceptionTask = () -> {
            exceptionCount.incrementAndGet();
            throw new RuntimeException();
        };

        assertThrows(
            RuntimeException.class,
            () -> ExceptionHelper.runAllAggregating(
                incrementTask,
                exceptionTask,
                incrementTask,
                exceptionTask,
                incrementTask
            )
        );

        assertEquals(3, count.get());
        assertEquals(2, exceptionCount.get());
    }

    @Test void allTasksRunOnNoException() {
        AtomicInteger count = new AtomicInteger();
        Runnable incrementTask = count::incrementAndGet;

        assertDoesNotThrow(() -> ExceptionHelper.runAllAggregating(incrementTask, incrementTask));
        assertEquals(2, count.get());
    }
}
