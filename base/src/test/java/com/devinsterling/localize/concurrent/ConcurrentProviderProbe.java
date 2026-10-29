package com.devinsterling.localize.concurrent;

import com.devinsterling.localize.ResourceBundleProvider;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentProviderProbe implements ResourceBundleProvider {
    private final ResourceBundleProvider delegate;
    private final Map<Locale, Integer> calls = new ConcurrentHashMap<>();
    private final CountDownLatch paused = new CountDownLatch(1);
    private final CountDownLatch resume = new CountDownLatch(1);
    private final AtomicBoolean armed = new AtomicBoolean();

    private volatile Locale pauseOn;
    private volatile Locale failOn;

    public ConcurrentProviderProbe(ResourceBundleProvider delegate) {
        this.delegate = delegate;
    }

    /// The first call matching the given `locale` blocks until [resume()].
    public ConcurrentProviderProbe pauseOn(Locale locale) {
        this.pauseOn = locale;
        this.armed.set(true);
        return this;
    }

    /// Every call to [getBundle] matching the given `locale` throws.
    public ConcurrentProviderProbe failOn(Locale locale) {
        this.failOn = locale;
        return this;
    }

    /// Blocks until some thread is parked inside this provider.
    public void awaitPaused() {
        await(paused, "provider never reached");
    }

    public void resume() {
        resume.countDown();
    }

    public int calls(Locale locale) {
        return calls.getOrDefault(locale, 0);
    }

    @Override public ResourceBundle getBundle(Locale locale) {
        calls.merge(locale, 1, Integer::sum);

        if (locale.equals(pauseOn) && armed.compareAndSet(true, false)) {
            paused.countDown();
            await(resume, "never resumed");
        } else if (locale.equals(failOn)) {
            throw new IllegalStateException("probe failure for " + locale);
        }

        return delegate.getBundle(locale);
    }

    private static void await(CountDownLatch latch, String message) {
        try {
            if (!latch.await(ConcurrentTestHelper.TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
