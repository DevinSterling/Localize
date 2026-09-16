package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class EventRegistryTest {

    @Test void testAddEventListener() {
        AtomicInteger count = new AtomicInteger();
        Localize localize = Localize.of(Locale.ENGLISH);
        EventListener<LocaleChangeEvent> listener = event -> count.incrementAndGet();

        localize.addListener(LocaleChangeEvent.class, listener);
        localize.addListener(LocaleChangeEvent.class, listener);
        localize.addListener(LocaleChangeEvent.class, listener);
        localize.setLocale(Locale.JAPANESE);
        assertEquals(1, count.get());

        localize.addBundleProvider(locale -> null);
        localize.setLocale(Locale.JAPANESE);
        assertEquals(1, count.get());

        localize.addListener(LocaleChangeEvent.class, listener);
        localize.setLocale(Locale.ENGLISH);
        assertEquals(2, count.get());
    }

    @Test void testRemoveEventListener() {
        AtomicInteger count = new AtomicInteger();
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize.ProviderKey key = Localize.ProviderKey.of();
        EventListener<ProviderChangeEvent> listener = event -> count.incrementAndGet();

        localize.addListener(ProviderChangeEvent.class, listener);
        localize.addListener(ProviderChangeEvent.class, listener);
        localize.putBundleProvider(key, locale -> null);
        assertEquals(1, count.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals(1, count.get());
        assertTrue(localize.removeListener(ProviderChangeEvent.class, listener));
        assertFalse(localize.removeListener(ProviderChangeEvent.class, listener));

        localize.removeBundleProvider(key);
        assertEquals(1, count.get());

        Subscription subscription = localize.addListener(ProviderChangeEvent.class, listener);
        localize.putBundleProvider(key, TEST_PROVIDER);
        assertEquals(2, count.get());

        subscription.dispose();
        subscription.dispose();
        localize.putBundleProvider(key, TEST2_PROVIDER);
        assertEquals(2, count.get());
    }

    @Test void testSubscription() {
        Localize localize = Localize.of(Locale.ENGLISH);
        EventListener<LocalizeEvent> listener = event -> {};

        Subscription a = localize.addListener(LocalizeEvent.class, listener);
        Subscription b = localize.addListener(LocalizeEvent.class, listener);
        assertSame(a, b);
        assertTrue(a.isActive());

        a.dispose();
        assertFalse(a.isActive());

        Subscription c = localize.addListener(LocalizeEvent.class, listener);
        a.dispose();
        assertNotSame(a, c);
        assertTrue(c.isActive());

        c.dispose();
        assertFalse(c.isActive());
    }

    @Test void testCustomEvents() {
        class LocalizeTest extends Localize {
            final AtomicInteger count = new AtomicInteger();

            protected LocalizeTest() {
                super(Localize.of());
            }

            public void fireTestEvent() {
                fireEvent(new Event(this, count.get()));
            }

            record Event(LocalizeTest source, int num) implements LocalizeEvent {
                @Override public LocalizeTest getSource() {
                    return source;
                }
            }
        }

        LocalizeTest localize = new LocalizeTest();
        EventListener<LocalizeTest.Event> listener =event -> {
            assertEquals(event.num, event.source.count.getAndIncrement());
        };

        Subscription subscription = localize.addListener(LocalizeTest.Event.class, listener);
        localize.fireTestEvent();
        localize.fireTestEvent();
        assertEquals(2, localize.count.get());

        subscription.dispose();
        localize.fireTestEvent();
        assertEquals(2, localize.count.get());
    }
}
