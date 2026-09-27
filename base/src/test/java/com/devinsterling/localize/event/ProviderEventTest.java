package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.ResourceBundleProvider;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderEventTest {
    private static final ResourceBundleProvider NULL_PROVIDER = locale -> null;

    @Test void testOnProviderPut() {
        LocalizeEventCapturer<ProviderEvent.Added> localize = new LocalizeEventCapturer<>(ProviderEvent.Added.class);

        Localize.ProviderKey key1 = Localize.ProviderKey.of();
        localize.putProvider(key1, NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry1 = localize.getProviderEntry(key1);

        Localize.ProviderKey key2 = Localize.ProviderKey.of("123");
        localize.putProvider(key2, NULL_PROVIDER); // 1
        Localize.ProviderEntry entry2 = localize.getProviderEntry(key2);

        Localize.ProviderEntry entry3 = localize.addProvider(NULL_PROVIDER); // 2

        assertEquals(3, localize.capturedEventsCount());
        assertSame(entry1, localize.getEvent(0).getEntry());
        assertSame(entry2, localize.getEvent(1).getEntry());
        assertSame(entry3, localize.getEvent(2).getEntry());
    }

    @Test void testOnProviderReplaced() {
        LocalizeEventCapturer<ProviderEvent.Replaced> localize
                = new LocalizeEventCapturer<>(ProviderEvent.Replaced.class);

        Localize.ProviderEntry entry1 = localize.addProvider(NULL_PROVIDER);
        localize.putProvider(entry1.getKey(), NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry1A = localize.getProviderEntry(entry1.getKey());
        localize.putProvider(entry1.getKey(), NULL_PROVIDER); // 1
        Localize.ProviderEntry entry1B = localize.getProviderEntry(entry1.getKey());

        localize.putProvider("abc", NULL_PROVIDER);
        Localize.ProviderEntry entry2 = localize.getProviderEntry("abc");
        localize.putProvider("abc", NULL_PROVIDER); // 2
        Localize.ProviderEntry entry2A = localize.getProviderEntry("abc");

        assertEquals(3, localize.capturedEventsCount());

        ProviderEvent.Replaced replacedEvent1 = localize.getEvent(0);
        assertSame(entry1, replacedEvent1.getOldEntry());
        assertSame(entry1A, replacedEvent1.getNewEntry());
        assertNotEquals(replacedEvent1.getNewEntry(), replacedEvent1.getOldEntry());

        ProviderEvent.Replaced replacedEvent2 = localize.getEvent(1);
        assertSame(entry1A, replacedEvent2.getOldEntry());
        assertSame(entry1B, replacedEvent2.getNewEntry());
        assertNotEquals(replacedEvent2.getNewEntry(), replacedEvent2.getOldEntry());

        ProviderEvent.Replaced replacedEvent3 = localize.getEvent(2);
        assertSame(entry2, replacedEvent3.getOldEntry());
        assertSame(entry2A, replacedEvent3.getNewEntry());
        assertNotEquals(replacedEvent3.getNewEntry(), replacedEvent3.getOldEntry());
    }

    @Test void testOnProviderRemove() {
        LocalizeEventCapturer<ProviderEvent.Removed> localize
                = new LocalizeEventCapturer<>(ProviderEvent.Removed.class);

        localize.removeProvider(Localize.ProviderKey.of());

        Localize.ProviderEntry entry1 = localize.addProvider(NULL_PROVIDER);
        Localize.ProviderEntry entry2 = localize.addProvider(NULL_PROVIDER);

        localize.removeProvider(entry2.getKey()); // Event 1
        localize.removeProvider(entry2.getKey());
        localize.removeProvider(Localize.ProviderKey.of());
        entry1.remove(); // 2
        entry1.remove();

        assertEquals(2, localize.capturedEventsCount());
        assertSame(entry2, localize.getEvent(0).getEntry());
        assertSame(entry1, localize.getEvent(1).getEntry());
    }

    @Test void testOnProviderBulkRemove() {
        LocalizeEventCapturer<ProviderEvent> localize = new LocalizeEventCapturer<>(ProviderEvent.class);

        localize.clearProviders();

        Localize.ProviderEntry entry1 = localize.addProvider(NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry2 = localize.addProvider(NULL_PROVIDER); // 1
        localize.clearProviders(); // 2

        Localize.ProviderEntry entry3 = localize.addProvider(NULL_PROVIDER); // 3
        localize.clearProviders(); // 4
        localize.clearProviders();

        assertEquals(5, localize.capturedEventsCount());
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(2, ProviderEvent.BulkRemoved.class).getEntries()
        );
        assertEquals(
            entry3,
            localize.getEvent(4, ProviderEvent.Removed.class).getEntry()
        );
    }

    @Test void testOnProviderRefresh() {
        LocalizeEventCapturer<ProviderEvent.Refreshed> localize
                = new LocalizeEventCapturer<>(ProviderEvent.Refreshed.class);

        localize.refreshProvider(Localize.ProviderKey.of());

        Localize.ProviderEntry entry1 = localize.addProvider(NULL_PROVIDER);
        Localize.ProviderEntry entry2 = localize.addProvider(NULL_PROVIDER);
        Localize.ProviderEntry entry3 = localize.addProvider(TEST_PROVIDER);
        Localize.ProviderEntry entry4 = localize.addProvider(TEST_PROVIDER);

        // No refreshes occur here because `NULL_PROVIDER` always returns a null bundle (Nothing to refresh)
        localize.refreshProvider(entry2.getKey());
        entry2.remove();
        entry2.refresh();
        entry1.refresh();

        // These refreshes occur since the returned bundle is always non-null (Something to refresh)
        localize.refreshProvider(entry3.getKey()); // Event 1
        entry3.refresh(); // 2
        entry3.remove();
        entry3.refresh();
        entry4.refresh(); // 3

        assertEquals(3, localize.capturedEventsCount());
        assertSame(entry3, localize.getEvent(0).getEntry());
        assertSame(entry3, localize.getEvent(1).getEntry());
        assertSame(entry4, localize.getEvent(2).getEntry());
    }

    @Test void testOnProviderBulkRefresh() {
        LocalizeEventCapturer<ProviderEvent> localize = new LocalizeEventCapturer<>(ProviderEvent.class);

        // No refreshes occur here because `NULL_PROVIDER` always returns a null bundle (Nothing to refresh)
        localize.addProvider(NULL_PROVIDER); // Event 0
        localize.refreshProviders();
        localize.addProvider(NULL_PROVIDER); // 1
        localize.refreshProviders();

        // These refreshes occur since the returned bundle is always non-null (Something to refresh)
        Localize.ProviderEntry entry3 = localize.addProvider(TEST_PROVIDER); // 2
        localize.refreshProviders(); // 3
        Localize.ProviderEntry entry4 = localize.addProvider(TEST_PROVIDER); // 4
        localize.refreshProviders(); // 5

        assertEquals(6, localize.capturedEventsCount());
        assertEquals(
            entry3,
            localize.getEvent(3, ProviderEvent.Refreshed.class).getEntry()
        );
        assertEquals(
            List.of(entry3, entry4),
            localize.getEvent(5, ProviderEvent.BulkRefreshed.class).getEntries()
        );
    }

    @Test void testSetLocaleOnProviderRefresh() {
        LocalizeEventCapturer<ProviderEvent> localize = new LocalizeEventCapturer<>(ProviderEvent.class);

        localize.setLocale(Locale.JAPANESE);
        localize.addProvider(NULL_PROVIDER); // Event 0
        localize.setLocale(Locale.CHINESE);

        Localize.ProviderEntry entry1 = localize.addProvider(TEST_PROVIDER); // 1
        localize.setLocale(Locale.ENGLISH); // 2

        Localize.ProviderEntry entry2 = localize.addProvider(TEST_PROVIDER); // 3
        localize.setLocale(Locale.JAPANESE); // 4
        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.CHINESE); // 5
        localize.clearProviders(); // 6
        localize.setLocale(Locale.ENGLISH);

        assertEquals(7, localize.capturedEventsCount());
        assertEquals(
            entry1,
            localize.getEvent(2, ProviderEvent.Refreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntry()
        );
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(4, ProviderEvent.BulkRefreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntries()
        );
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(5, ProviderEvent.BulkRefreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntries()
        );
    }

    @Test void testOnProviderExceptionCaught() {
        class ProviderTestException extends RuntimeException {}

        LocalizeEventCapturer<ProviderEvent.ExceptionCaught> localize
                = new LocalizeEventCapturer<>(ProviderEvent.ExceptionCaught.class);

        ProviderTestException providerException = new ProviderTestException();
        ResourceBundleProvider exceptionProvider = locale -> {
            throw providerException;
        };

        // Event 0
        assertThrows(ProviderTestException.class, () -> localize.addProvider(exceptionProvider));
        assertThrows(ProviderTestException.class, localize::refreshProviders); // Event 1

        ProviderEvent.ExceptionCaught event0 = localize.getEvent(0);
        assertSame(providerException, event0.getException());
        assertSame(exceptionProvider, event0.getProvider());
        assertSame(Locale.ENGLISH, event0.getLocale());

        ProviderEvent.ExceptionCaught event1 = localize.getEvent(1);
        assertEquals(event0, event1);
    }
}
