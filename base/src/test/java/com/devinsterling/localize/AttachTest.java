package com.devinsterling.localize;

import com.devinsterling.localize.event.LocaleEvent;
import com.devinsterling.localize.event.LocalizeEvent;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class AttachTest {

    @Test void testBasicAttach() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize attached1 = localize.attach(Localize::new);
        Localize attached2 = localize.attach(Localize::new);

        assertNotEquals(localize, attached1);
        assertNotEquals(attached1, attached2);
        assertEquals(Locale.ENGLISH, attached1.getLocale());

        attached2.setLocale(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, attached2.getLocale());
        assertEquals(Locale.JAPANESE, localize.getLocale());

        assertEquals(localize.getProviderEntries(), attached1.getProviderEntries());
    }

    @Test void testNestedAttach() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize attached1 = localize.attach(Localize::new);
        Localize attached2 = attached1.attach(Localize::new);

        assertEquals(Locale.ENGLISH, attached2.getLocale());

        localize.setLocale(Locale.CHINESE);
        assertEquals(Locale.CHINESE, attached1.getLocale());
        assertEquals(Locale.CHINESE, attached2.getLocale());
    }

    @SuppressWarnings({ "unused", "UnusedAssignment" })
    @Test void testAttachGarbageCollection() {
        Localize source = Localize.of(Locale.ENGLISH);
        WeakReference<Localize> weak = new WeakReference<>(source.attach(Localize::new));
        WeakReference<Localize> weakNested = new WeakReference<>(weak.get().attach(Localize::new));

        awaitGarbageCollection();

        assertNull(weak.get());
        assertNull(weakNested.get());

        weak = new WeakReference<>(source);
        Localize attached = source.attach(Localize::new);
        source = null;

        awaitGarbageCollection();

        // Internally, attached holds a strong reference to its "parent" (`source`)
        // So the weak reference is not GC'd here
        assertNotNull(weak.get());
    }

    @Test void preventSourceEscapingAttachMethodScope() {
        AtomicReference<Localize.Source> source = new AtomicReference<>();
        Localize localize = Localize.of();

        // Throwing an exception by throwing an exception
        assertThrows(RuntimeException.class, () -> localize.attach(s -> {
            source.set(s);
            throw new RuntimeException();
        }));
        assertNotNull(source.get());
        assertThrows(IllegalStateException.class, () -> new Localize(source.get()));
    }

    @Test void throwOnFactoryReturningNull() {
        Localize localize = Localize.of();
        assertThrows(NullPointerException.class, () -> localize.attach(s -> null));
    }

    @Test void throwOnUnattached() {
        Localize localize = Localize.of();
        assertThrows(IllegalStateException.class, () -> localize.attach(s -> Localize.of()));
    }

    @Test void throwOnMismatch() {
        Localize localize = Localize.of();
        assertThrows(IllegalStateException.class, () -> localize.attach(s -> {
            // This will work, although an exception will later be thrown to notify that a mismatch occurred.
            // NOTE: As a sideeffect, this instance will not receive any events from other attached instances.
            assertDoesNotThrow(() -> new Localize(s));

            return Localize.of();
        }));
    }

    @Test void throwOnMultipleUses() {
        Localize localize = Localize.of();
        localize.attach(s -> {
            Localize instance = new Localize(s);

            // This will throw as the source has already been used by another instance
            assertThrows(IllegalStateException.class, () -> new Localize(s));
            return instance;
        });
    }

    @Test void detachOnSyntheticEventException() {
        Localize localize = Localize.of(Locale.ENGLISH);

        // If the faulty throws an exception within the `attach` method,
        // it must be detached to prevent a "zombie" instance from receiving events.
        assertThrows(UnsupportedOperationException.class, () -> localize.attach(s -> new Localize(s) {
            @Override protected void onLocaleReplaced(LocaleEvent.Replaced event) {
                throw new UnsupportedOperationException();
            }
        }));

        assertDoesNotThrow(() -> localize.setLocale(Locale.JAPANESE));
    }

    @Test void receiveSyntheticLocaleReplacedEvent() {
        Localize localize = Localize.of(Locale.ENGLISH);
        AtomicReference<LocaleEvent.Replaced> capturedEvent = new AtomicReference<>();

        localize.attach(s -> new Localize(s) {
            @Override protected void onLocaleReplaced(LocaleEvent.Replaced event) {
                capturedEvent.set(event);
            }
        });

        LocaleEvent.Replaced event = capturedEvent.get();

        assertNotNull(event);
        assertEquals(LocalizeEvent.Cause.ATTACHED, event.getCause());
        assertSame(localize.getLocale(), event.getOldLocale());
        assertSame(localize.getLocale(), event.getNewLocale());
        assertTrue(event.isValid());
    }

    @Test void otherListenersDoNotReceiveSyntheticLocaleReplacedEvent() {
        AtomicInteger count = new AtomicInteger();
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onLocaleReplaced(LocaleEvent.Replaced event) {
                count.incrementAndGet();
            }
        };

        localize.addListener(LocaleEvent.Replaced.class, event -> count.incrementAndGet());
        localize.attach(Localize::new);

        assertEquals(0, count.get());
    }
}
