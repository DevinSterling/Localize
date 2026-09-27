package com.devinsterling.localize;

import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.FormatterEvent;
import com.devinsterling.localize.event.LocaleEvent;
import com.devinsterling.localize.event.LocalizeEvent;
import com.devinsterling.localize.event.ProviderEvent;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeHookTest {

    @Test void testOnLocaleReplaced() {
        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onLocaleReplaced(LocaleEvent.Replaced event) {
                counter.incrementAndGet();
            }
        };

        assertEquals(0, counter.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals(1, counter.get());

        localize.setLocale(Locale.FRENCH);
        localize.setLocale(Locale.ENGLISH);
        assertEquals(3, counter.get());

        localize.setLocale(Locale.CHINESE);
        localize.setLocale(Locale.CHINESE);
        assertThrows(NullPointerException.class, () -> localize.setLocale(null));

        localize.setLocale(Locale.CHINESE);
        assertEquals(4, counter.get());
    }

    @Test void testOnFormatterReplaced() {
        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onFormatterReplaced(FormatterEvent.Replaced event) {
                counter.incrementAndGet();
            }
        };
        LocalizationFormatter nullFormatter = ctx -> null;

        assertEquals(0, counter.get());

        localize.setFormatter(nullFormatter);
        localize.setFormatter(nullFormatter);
        localize.setFormatter(nullFormatter);
        assertEquals(1, counter.get());

        localize.setFormatter(LocalizationFormatter.STANDARD);
        localize.setFormatter(LocalizationFormatter.STANDARD);
        assertEquals(2, counter.get());

        localize.setFormatter(nullFormatter);
        assertEquals(3, counter.get());
    }

    @Test void testOnProvidersChange() {
        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onProviderEvent(ProviderEvent event) {
                counter.incrementAndGet();
            }
        };

        assertEquals(0, counter.get());

        Localize.ProviderEntry entry1 = localize.addProvider(TEST_PROVIDER_NAME);
        assertEquals(1, counter.get());

        localize.addProvider(TEST_PROVIDER);
        localize.putProvider("key", TEST2_PROVIDER);
        localize.putProvider("key", TEST2_PROVIDER_NAME);
        localize.putProvider(Localize.ProviderKey.of("key2"), TEST_PROVIDER);
        assertEquals(5, counter.get());

        localize.removeProvider("non-existent key");
        localize.refreshProvider("non-existent key");
        assertEquals(5, counter.get());

        localize.removeProvider("key");
        localize.refreshProvider("key2");
        assertEquals(7, counter.get());

        localize.refreshProviders();
        assertEquals(8, counter.get());

        entry1.refresh();
        entry1.remove();
        assertEquals(10, counter.get());

        entry1.refresh();
        entry1.remove();
        assertEquals(10, counter.get());

        localize.clearProviders();
        assertEquals(11, counter.get());

        localize.clearProviders();
        assertEquals(11, counter.get());
    }

    @Test void testCatchingHookExceptionsAreNotRecursive() {
        class TestException extends RuntimeException {}

        ArrayList<LocalizeEvent> caughtEvents = new ArrayList<>();
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onEvent(LocalizeEvent event) {
                caughtEvents.add(event);
                throw new TestException();
            }
        };

        assertThrows(TestException.class, () -> localize.addProvider(TEST_PROVIDER));

        assertEquals(2, caughtEvents.size());
        assertInstanceOf(ProviderEvent.Added.class, caughtEvents.get(0));
        // `ListenerExceptionCaught` - cannot be recursive.
        assertInstanceOf(DiagnosticEvent.ListenerExceptionCaught.class, caughtEvents.get(1));

        localize.getConfig().setIgnoreListenerExceptions(true);
        localize.addProvider(TEST_PROVIDER);

        assertEquals(4, caughtEvents.size());
        assertInstanceOf(ProviderEvent.Added.class, caughtEvents.get(2));
        assertInstanceOf(DiagnosticEvent.ListenerExceptionCaught.class, caughtEvents.get(3));
    }

    @Test void testAttachingHooks() {
        record TestEvent(Localize getSource) implements LocalizeEvent {}

        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = Localize.of();
        WeakReference<Localize> attached1 = new WeakReference<>(new Localize(localize) {
            @Override protected void onEvent(LocalizeEvent event) {
                counter.incrementAndGet();
            }
        });
        WeakReference<Localize> attached2 = new WeakReference<>(new Localize(localize) {
            @Override protected void onEvent(LocalizeEvent event) {
                counter.incrementAndGet();
            }
        });

        localize.fireEvent(new TestEvent(localize));
        assertEquals(2, counter.get());

        awaitGarbageCollection();
        assertNull(attached1.get());
        assertNull(attached2.get());

        localize.fireEvent(new TestEvent(localize));
        assertEquals(2, counter.get());
    }
}
