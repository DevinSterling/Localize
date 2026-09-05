package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class SubscriptionTest {

    @Test void testEmptySubscription() {
        Subscription subscription = Subscription.EMPTY;

        // Always false for an `EMPTY` subscription.
        assertFalse(subscription.isActive());

        // dispose is a no-op
        subscription.dispose();
        assertFalse(subscription.isActive());
    }

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

    @Test void testSubscriptionAnd() {
        Subscription a = Subscription.of(() -> {});
        Subscription b = Subscription.of(() -> {});
        Subscription ab = a.and(b);

        assertTrue(ab.isActive());

        b.dispose();
        assertTrue(ab.isActive());

        a.dispose();
        assertFalse(ab.isActive());

        Subscription c = Subscription.of(() -> {});
        Subscription d = Subscription.of(() -> {});
        Subscription cd = c.and(d);
        Subscription cd2 = c.and(d);

        cd.dispose();
        assertFalse(cd.isActive());
        assertFalse(cd2.isActive());
        assertFalse(c.isActive());
        assertFalse(d.isActive());

    }

    @Test void testSubscriptionCombine() {
        Subscription a = Subscription.EMPTY;
        Subscription b = Subscription.of(() -> {});
        Subscription ab = Subscription.combine(a, b);

        assertTrue(ab.isActive());

        b.dispose();
        assertFalse(ab.isActive());

        Subscription c = Subscription.of(() -> {});
        Subscription d = Subscription.of(() -> {});
        Subscription cd = Subscription.combine(c, d);
        Subscription cd2 = Subscription.combine(c, d);

        cd.dispose();
        assertFalse(cd.isActive());
        assertFalse(cd2.isActive());
        assertFalse(c.isActive());
        assertFalse(d.isActive());
    }

    @Test void testSubscriptionAndCombineNonNull() {
        assertThrows(NullPointerException.class, () -> Subscription.EMPTY.and(null));
        assertThrows(NullPointerException.class, () -> Subscription.combine((Subscription[]) null));
        assertThrows(NullPointerException.class, () -> Subscription.combine(Subscription.EMPTY, null));
    }

}
