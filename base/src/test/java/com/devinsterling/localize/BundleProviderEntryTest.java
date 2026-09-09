package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class BundleProviderEntryTest {

    @Test void testPutProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize.ProviderKey key = Localize.ProviderKey.of("key");

        localize.putBundleProvider(key, TEST_PROVIDER);
        Localize.ProviderEntry entry = localize.getBundleProviderEntry(key);

        assertSame(key, entry.getKey());
        assertEquals(TEST_PROVIDER, entry.getProvider());
        assertEquals(ResourceBundle.getBundle(TEST_PROVIDER_NAME), entry.getBundle());
        assertTrue(entry.isActive());

        localize.putBundleProvider(key, TEST_PROVIDER);
        Localize.ProviderEntry newEntry = localize.getBundleProviderEntry(key);

        assertFalse(entry.isActive());
        assertTrue(newEntry.isActive());
    }

    @Test void testAddProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);

        // Both keys are unique
        Localize.ProviderEntry entry1 = localize.addBundleProvider(TEST_PROVIDER);
        Localize.ProviderEntry entry2 = localize.addBundleProvider(TEST_PROVIDER);

        assertNotEquals(entry1, entry2);
        assertTrue(entry1.isActive());
        assertTrue(entry2.isActive());
        assertSame(TEST_PROVIDER, entry1.getProvider());
        assertEquals(ResourceBundle.getBundle(TEST_PROVIDER_NAME), entry1.getBundle());
    }

    @Test void testRemoveProviderEntry() {
        Localize localize = Localize.of(Locale.ENGLISH);

        Localize.ProviderEntry entry1 = localize.addBundleProvider(TEST_PROVIDER);
        assertNotNull(entry1.getBundle());

        entry1.remove();
        assertFalse(entry1.isActive());
        assertNull(entry1.getBundle());
        assertNull(localize.getBundleProviderEntry(entry1.getKey()));

        entry1.remove();
        assertFalse(entry1.isActive());

        Localize.ProviderKey key = Localize.ProviderKey.of("key");
        localize.putBundleProvider(key, TEST2_PROVIDER);
        Localize.ProviderEntry entry2 = localize.getBundleProviderEntry(key);
        entry2.remove();
        entry2.remove();

        assertFalse(entry2.isActive());
        assertNull(entry2.getBundle());
        assertNull(localize.getBundleProviderEntry(entry2.getKey()));

        Localize.ProviderEntry entry3 = localize.addBundleProvider(TEST_PROVIDER);
        assertTrue(entry3.isActive());

        localize.removeBundleProvider(entry3.getKey());
        assertFalse(entry3.isActive());
    }

    @Test void testRefreshProviderEntry() {
        class CountBundleProvider extends ResourceBundle implements ResourceBundleProvider {
            final AtomicInteger count = new AtomicInteger();

            @Override protected Object handleGetObject(String key) {
                return String.valueOf(count);
            }
            @Override public Enumeration<String> getKeys() {
                return Collections.enumeration(List.of(TEST_KEY_TEST));
            }
            @Override public ResourceBundle getBundle(Locale locale) {
                count.incrementAndGet();
                return this;
            }
        }

        Localize localize = Localize.of(Locale.ENGLISH);
        localize.getConfig().setDefaultMissingValue(null);
        Localize.ProviderEntry entry = localize.addBundleProvider(new CountBundleProvider());

        assertEquals("1", localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        assertEquals("2", localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        entry.refresh();
        assertEquals("4", localize.getValue(TEST_KEY_TEST));

        localize.refresh(entry.getKey());
        entry.refresh();
        assertEquals("6", localize.getValue(TEST_KEY_TEST));

        entry.remove();
        assertNull(localize.getValue(TEST_KEY_TEST));

        entry.refresh();
        assertNull(localize.getValue(TEST_KEY_TEST));
    }
}
