package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class SubscriptionTest {

    @Test void testEmptySubscriptionReturned() {
        LocalizeSwing localize = LocalizeSwing.of();

        // Maintain parity with Swing by not throwing an NPE when adding `null` listeners.
        // Instead, `Subscription.EMPTY` is returned:
        assertSame(Subscription.EMPTY, localize.addLocaleListener(null));
        assertSame(Subscription.EMPTY, localize.addPropertyChangeListener(null));
    }

    @Test void testPropertyChangeSubscriptionActive() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
        AtomicInteger count = new AtomicInteger();

        Subscription subscription = localize.addLocaleListener((a, b) -> count.incrementAndGet());

        assertTrue(subscription.isActive());
        assertEquals(0, count.get());

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.ENGLISH);
        localize.setLocale(Locale.ENGLISH);

        assertEquals(2, count.get());
    }

    @Test void testPropertyChangeSubscriptionDispose() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
        AtomicInteger count = new AtomicInteger();

        Subscription subscription = localize.addLocaleListener((a, b) -> count.incrementAndGet());

        subscription.dispose();
        assertFalse(subscription.isActive());

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.ENGLISH);

        assertEquals(0, count.get());
    }
}
