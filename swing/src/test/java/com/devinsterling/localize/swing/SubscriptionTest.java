package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.swing.fixtures.PropertyChangeListenerProbe;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
class SubscriptionTest {

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

    @Test void captureAggregateExceptions() {
        PropertyChangeManager manager = new PropertyChangeManager();

        var a = new PropertyChangeListenerProbe().throwOnCall(true);
        var b = new PropertyChangeListenerProbe();
        var c = new PropertyChangeListenerProbe().throwOnCall(true);
        var d = new PropertyChangeListenerProbe();

        manager.addPropertyChangeListener(a);
        manager.addPropertyChangeListener(b);
        manager.addPropertyChangeListener(c);
        manager.addPropertyChangeListener(d);

        var exception = assertThrows(
            PropertyChangeListenerProbe.ListenerException.class,
            () -> manager.notifyChangeListeners(this, Locale.ENGLISH, Locale.FRENCH)
        );

        assertSame(a.exception, exception);
        assertArrayEquals(new Throwable[] { c.exception }, exception.getSuppressed());

        // Despite the exception, valid listeners are still called
        assertEquals(1, b.count);
        assertEquals(1, d.count);
    }

    @Test void aggregateExceptionCannotSuppressItself() {
        PropertyChangeManager manager = new PropertyChangeManager();
        RuntimeException exception = new RuntimeException();

        manager.addPropertyChangeListener(evt -> {
            throw exception;
        });
        manager.addPropertyChangeListener(evt -> {
            throw exception;
        });

        var caught = assertThrows(
            RuntimeException.class,
            () -> manager.notifyChangeListeners(this, Locale.ENGLISH, Locale.FRENCH)
        );

        assertSame(caught, exception);
        assertEquals(0, caught.getSuppressed().length);
    }

    @Test void listenerRemovedMidDispatchIsNotInvoked() {
        PropertyChangeManager manager = new PropertyChangeManager();
        AtomicReference<Subscription> subscriptionC = new AtomicReference<>();

        var d = new PropertyChangeListenerProbe();
        var c = new PropertyChangeListenerProbe();
        var b = new PropertyChangeListenerProbe();
        var a = new PropertyChangeListenerProbe(evt -> {
            manager.removePropertyChangeListener(b);
            subscriptionC.get().dispose();
        });

        manager.addPropertyChangeListener(a);
        manager.addPropertyChangeListener(b);
        subscriptionC.set(manager.addPropertyChangeListener(c));
        manager.addPropertyChangeListener(d);
        manager.notifyChangeListeners(this, Locale.ENGLISH, Locale.FRENCH);

        assertEquals(1, a.count);
        assertEquals(0, b.count);
        assertEquals(0, c.count);
        assertEquals(1, d.count);
    }
}
