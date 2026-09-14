package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.ResourceBundleProvider;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderChangeEventTest {
    private static final ResourceBundleProvider NULL_PROVIDER = locale -> null;

    @Test void testOnProviderPut() {
        LocalizeEventTest localize = new LocalizeEventTest();

        Localize.ProviderKey key1 = Localize.ProviderKey.of();
        localize.putBundleProvider(key1, NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry1 = localize.getBundleProviderEntry(key1);

        Localize.ProviderKey key2 = Localize.ProviderKey.of("123");
        localize.putBundleProvider(key2, NULL_PROVIDER); // 1
        Localize.ProviderEntry entry2 = localize.getBundleProviderEntry(key2);

        Localize.ProviderEntry entry3 = localize.addBundleProvider(NULL_PROVIDER); // 2

        assertEquals(3, localize.capturedEvents.size());
        assertSame(entry1, localize.getEvent(0, ProviderChangeEvent.Added.class).getEntry());
        assertSame(entry2, localize.getEvent(1, ProviderChangeEvent.Added.class).getEntry());
        assertSame(entry3, localize.getEvent(2, ProviderChangeEvent.Added.class).getEntry());
    }

    @Test void testOnProviderReplaced() {
        LocalizeEventTest localize = new LocalizeEventTest();

        Localize.ProviderEntry entry1 = localize.addBundleProvider(NULL_PROVIDER); // Event 0
        localize.putBundleProvider(entry1.getKey(), NULL_PROVIDER); // 1
        Localize.ProviderEntry entry1A = localize.getBundleProviderEntry(entry1.getKey());
        localize.putBundleProvider(entry1.getKey(), NULL_PROVIDER); // 2
        Localize.ProviderEntry entry1B = localize.getBundleProviderEntry(entry1.getKey());

        localize.putBundleProvider("abc", NULL_PROVIDER); // 3
        Localize.ProviderEntry entry2 = localize.getBundleProviderEntry("abc");
        localize.putBundleProvider("abc", NULL_PROVIDER); // 4
        Localize.ProviderEntry entry2A = localize.getBundleProviderEntry("abc");

        assertEquals(5, localize.capturedEvents.size());

        ProviderChangeEvent.Replaced replacedEvent1 = localize.getEvent(1, ProviderChangeEvent.Replaced.class);
        assertSame(entry1, replacedEvent1.getOldEntry());
        assertSame(entry1A, replacedEvent1.getNewEntry());
        assertNotEquals(replacedEvent1.getNewEntry(), replacedEvent1.getOldEntry());

        ProviderChangeEvent.Replaced replacedEvent2 = localize.getEvent(2, ProviderChangeEvent.Replaced.class);
        assertSame(entry1A, replacedEvent2.getOldEntry());
        assertSame(entry1B, replacedEvent2.getNewEntry());
        assertNotEquals(replacedEvent2.getNewEntry(), replacedEvent2.getOldEntry());

        ProviderChangeEvent.Replaced replacedEvent3 = localize.getEvent(4, ProviderChangeEvent.Replaced.class);
        assertSame(entry2, replacedEvent3.getOldEntry());
        assertSame(entry2A, replacedEvent3.getNewEntry());
        assertNotEquals(replacedEvent3.getNewEntry(), replacedEvent3.getOldEntry());
    }

    @Test void testOnProviderRemove() {
        LocalizeEventTest localize = new LocalizeEventTest();

        localize.removeBundleProvider(Localize.ProviderKey.of());

        Localize.ProviderEntry entry1 = localize.addBundleProvider(NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry2 = localize.addBundleProvider(NULL_PROVIDER); // 1

        localize.removeBundleProvider(entry2.getKey()); // 2
        localize.removeBundleProvider(entry2.getKey());
        localize.removeBundleProvider(Localize.ProviderKey.of());
        entry1.remove(); // 3
        entry1.remove();

        assertEquals(4, localize.capturedEvents.size());
        assertSame(entry2, localize.getEvent(2, ProviderChangeEvent.Removed.class).getEntry());
        assertSame(entry1, localize.getEvent(3, ProviderChangeEvent.Removed.class).getEntry());
    }

    @Test void testOnProviderBulkRemove() {
        LocalizeEventTest localize = new LocalizeEventTest();

        localize.clearBundleProviders();

        Localize.ProviderEntry entry1 = localize.addBundleProvider(NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry2 = localize.addBundleProvider(NULL_PROVIDER); // 1
        localize.clearBundleProviders(); // 2

        Localize.ProviderEntry entry3 = localize.addBundleProvider(NULL_PROVIDER); // 3
        localize.clearBundleProviders(); // 4
        localize.clearBundleProviders();

        assertEquals(5, localize.capturedEvents.size());
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(2, ProviderChangeEvent.BulkRemoved.class).getEntries()
        );
        assertEquals(
            entry3,
            localize.getEvent(4, ProviderChangeEvent.Removed.class).getEntry()
        );
    }

    @Test void testOnProviderRefresh() {
        LocalizeEventTest localize = new LocalizeEventTest();

        localize.refresh(Localize.ProviderKey.of());

        Localize.ProviderEntry entry1 = localize.addBundleProvider(NULL_PROVIDER); // Event 0
        Localize.ProviderEntry entry2 = localize.addBundleProvider(NULL_PROVIDER); // 1
        Localize.ProviderEntry entry3 = localize.addBundleProvider(TEST_PROVIDER); // 2
        Localize.ProviderEntry entry4 = localize.addBundleProvider(TEST_PROVIDER); // 3

        // No refreshes occur here because `NULL_PROVIDER` always returns a null bundle (Nothing to refresh)
        localize.refresh(entry2.getKey());
        entry2.remove(); // 4
        entry2.refresh();
        entry1.refresh();

        // These refreshes occur since the returned bundle is always non-null (Something to refresh)
        localize.refresh(entry3.getKey()); // 5
        entry3.refresh(); // 6
        entry3.remove(); // 7
        entry3.refresh();
        entry4.refresh(); // 8

        assertEquals(9, localize.capturedEvents.size());
        assertSame(entry3, localize.getEvent(5, ProviderChangeEvent.Refreshed.class).getEntry());
        assertSame(entry3, localize.getEvent(6, ProviderChangeEvent.Refreshed.class).getEntry());
        assertSame(entry4, localize.getEvent(8, ProviderChangeEvent.Refreshed.class).getEntry());
    }

    @Test void testOnProviderBulkRefresh() {
        LocalizeEventTest localize = new LocalizeEventTest();

        // No refreshes occur here because `NULL_PROVIDER` always returns a null bundle (Nothing to refresh)
        localize.addBundleProvider(NULL_PROVIDER); // Event 0
        localize.refresh();
        localize.addBundleProvider(NULL_PROVIDER); // 1
        localize.refresh();

        // These refreshes occur since the returned bundle is always non-null (Something to refresh)
        Localize.ProviderEntry entry3 = localize.addBundleProvider(TEST_PROVIDER); // 2
        localize.refresh(); // 3
        Localize.ProviderEntry entry4 = localize.addBundleProvider(TEST_PROVIDER); // 4
        localize.refresh(); // 5

        assertEquals(6, localize.capturedEvents.size());
        assertEquals(
            entry3,
            localize.getEvent(3, ProviderChangeEvent.Refreshed.class).getEntry()
        );
        assertEquals(
            List.of(entry3, entry4),
            localize.getEvent(5, ProviderChangeEvent.BulkRefreshed.class).getEntries()
        );
    }

    @Test void testSetLocaleOnProviderRefresh() {
        LocalizeEventTest localize = new LocalizeEventTest();

        localize.setLocale(Locale.JAPANESE);
        localize.addBundleProvider(NULL_PROVIDER); // Event 0
        localize.setLocale(Locale.CHINESE);

        Localize.ProviderEntry entry1 = localize.addBundleProvider(TEST_PROVIDER); // 1
        localize.setLocale(Locale.ENGLISH); // 2

        Localize.ProviderEntry entry2 = localize.addBundleProvider(TEST_PROVIDER); // 3
        localize.setLocale(Locale.JAPANESE); // 4
        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.CHINESE); // 5
        localize.clearBundleProviders(); // 6
        localize.setLocale(Locale.ENGLISH);

        assertEquals(7, localize.capturedEvents.size());
        assertEquals(
            entry1,
            localize.getEvent(2, ProviderChangeEvent.Refreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntry()
        );
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(4, ProviderChangeEvent.BulkRefreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntries()
        );
        assertEquals(
            List.of(entry1, entry2),
            localize.getEvent(5, ProviderChangeEvent.BulkRefreshed.class, LocalizeEvent.Cause.LOCALE_CHANGE).getEntries()
        );
    }

    private static final class LocalizeEventTest extends Localize {
        private final List<ProviderChangeEvent> capturedEvents = new ArrayList<>();

        private LocalizeEventTest() {
            super(Locale.ENGLISH, new LocalizeConfig());
        }

        @Override protected void onProvidersChanged(ProviderChangeEvent event) {
            capturedEvents.add(event);
        }

        private <E extends ProviderChangeEvent> E getEvent(int i, Class<E> expectedEvent) {
            return getEvent(i, expectedEvent, LocalizeEvent.Cause.EXTERNAL);
        }

        private <E extends ProviderChangeEvent> E getEvent(
            int i,
            Class<E> expectedEvent,
            LocalizeEvent.Cause cause
        ) {
            ProviderChangeEvent event = capturedEvents.get(i);

            assertSame(this, event.getSource());
            assertSame(cause, event.getCause());
            assertTrue(
                expectedEvent.isInstance(event),
            "Expected event at " + i + " should be " + expectedEvent.getSimpleName() +
                ", got " + event.getClass().getSimpleName()
            );

            return expectedEvent.cast(event);
        }
    }
}
