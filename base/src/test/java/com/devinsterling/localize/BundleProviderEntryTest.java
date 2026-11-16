package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class BundleProviderEntryTest {

    @Test void testPutProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize.ProviderKey key = Localize.ProviderKey.of("key");

        localize.putProvider(key, TEST_PROVIDER);
        Localize.ProviderEntry entry = localize.getProviderEntry(key);

        assertSame(key, entry.getKey());
        assertEquals(TEST_PROVIDER, entry.getProvider());
        assertEquals(PatternSource.of(ResourceBundle.getBundle(TEST_PROVIDER_NAME)), entry.getPatternSource());
        assertTrue(entry.isActive());

        localize.putProvider(key, TEST_PROVIDER);
        Localize.ProviderEntry newEntry = localize.getProviderEntry(key);

        assertFalse(entry.isActive());
        assertTrue(newEntry.isActive());
    }

    @Test void testAddProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);

        // Both keys are unique
        Localize.ProviderEntry entry1 = localize.addProvider(TEST_PROVIDER);
        Localize.ProviderEntry entry2 = localize.addProvider(TEST_PROVIDER);

        assertNotEquals(entry1, entry2);
        assertTrue(entry1.isActive());
        assertTrue(entry2.isActive());
        assertSame(TEST_PROVIDER, entry1.getProvider());
        assertEquals(PatternSource.of(ResourceBundle.getBundle(TEST_PROVIDER_NAME)), entry1.getPatternSource());
    }

    @Test void testRemoveProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);

        Localize.ProviderEntry entry1 = localize.addProvider(TEST_PROVIDER);
        assertNotNull(entry1.getPatternSource());

        entry1.remove();
        assertFalse(entry1.isActive());
        assertNotNull(entry1.getPatternSource());
        assertNull(localize.getProviderEntry(entry1.getKey()));

        entry1.remove();
        assertFalse(entry1.isActive());

        Localize.ProviderKey key = Localize.ProviderKey.of("key");
        localize.putProvider(key, TEST2_PROVIDER);
        Localize.ProviderEntry entry2 = localize.getProviderEntry(key);
        entry2.remove();
        entry2.remove();

        assertFalse(entry2.isActive());
        assertNotNull(entry2.getPatternSource());
        assertNull(localize.getProviderEntry(entry2.getKey()));

        Localize.ProviderEntry entry3 = localize.addProvider(TEST_PROVIDER);
        assertTrue(entry3.isActive());

        localize.removeProvider(entry3.getKey());
        assertFalse(entry3.isActive());
    }

    @Test void testRefreshProviderEntry() {
        AtomicInteger count = new AtomicInteger();
        PatternSourceProvider countProvider = locale -> {
            count.incrementAndGet();
            return key -> String.valueOf(count.get());
        };

        Localize localize = Localize.of(Locale.ENGLISH);
        localize.getConfig().setDefaultMissingValue(null);
        Localize.ProviderEntry entry = localize.addProvider(countProvider);

        assertEquals("1", localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        assertEquals("2", localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        entry.refresh();
        assertEquals("4", localize.getValue(TEST_KEY_TEST));

        localize.refreshProvider(entry.getKey());
        entry.refresh();
        assertEquals("6", localize.getValue(TEST_KEY_TEST));

        entry.remove();
        assertNull(localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        assertNull(localize.getValue(TEST_KEY_TEST));
    }
}
