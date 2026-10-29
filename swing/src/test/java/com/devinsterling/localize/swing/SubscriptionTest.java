package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test void listenerRemovedMidDispatchIsNotInvoked() {
        PropertyChangeManager manager = new PropertyChangeManager();
        AtomicReference<Subscription> subscriptionC = new AtomicReference<>();

        var d = PropertyChangeCountListener.doesNothing();
        var c = PropertyChangeCountListener.doesNothing();
        var b = PropertyChangeCountListener.doesNothing();
        var a = new PropertyChangeCountListener(evt -> {
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

    private static final class PropertyChangeCountListener implements PropertyChangeListener {
        private final PropertyChangeListener delegate;
        private int count = 0;

        private PropertyChangeCountListener(PropertyChangeListener delegate) {
            this.delegate = delegate;
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            count++;
            delegate.propertyChange(evt);
        }

        private static PropertyChangeCountListener doesNothing() {
            return new PropertyChangeCountListener(evt -> {});
        }
    }
}
