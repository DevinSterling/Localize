package com.devinsterling.localize;

import com.devinsterling.localize.concurrent.ConcurrentProviderProbe;
import com.devinsterling.localize.concurrent.ConcurrentTestHelper;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizeTest {

    @Test void testDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        assertEquals(defaultLocale, Localize.of().getLocale());
        assertEquals(defaultLocale, Localize.of(new LocalizeConfig()).getLocale());
    }

    @Test void testDefaultConfig() {
        LocalizeConfig defaultConfig = new LocalizeConfig();
        assertEquals(defaultConfig, Localize.of().getConfig());
        assertEquals(defaultConfig, Localize.of(Locale.ENGLISH).getConfig());
    }

    @Test void testPutProvider() {
        Localize localize = Localize.of();
        Collection<Localize.ProviderEntry> entries = localize.getProviderEntries();
        String key = "key";

        assertTrue(entries.isEmpty());
        assertNull(localize.putProvider(key, TEST_PROVIDER));
        assertSame(TEST_PROVIDER, localize.putProvider(key, TEST2_PROVIDER));
        assertEquals(1, entries.size());

        assertSame(TEST2_PROVIDER, localize.removeProvider(key));
        assertTrue(entries.isEmpty());

        assertNull(localize.putProvider(key, TEST_PROVIDER));
        assertNull(localize.putProvider("other", TEST_PROVIDER));
        assertEquals(2, entries.size());
    }

    @Test void testPutProviderByBaseName() {
        Localize localize = Localize.of();
        String key = "key";

        // NOTE: these methods delegate to `putProvider(String, ResourceBundleProvider)`
        localize.putProvider(key, TEST_PROVIDER_NAME);
        localize.putProvider(key, TEST2_PROVIDER_NAME);
        localize.putProvider("other", TEST2_PROVIDER_NAME);
        assertEquals(2, localize.getProviderEntries().size());
    }

    @Test void testAddProvider() {
        Localize localize = Localize.of();
        Collection<Localize.ProviderEntry> entries = localize.getProviderEntries();

        Localize.ProviderEntry entry1 = localize.addProvider(TEST_PROVIDER);
        Localize.ProviderEntry entry2 = localize.addProvider(TEST2_PROVIDER);
        assertEquals(2, entries.size());

        entry1.remove();
        assertEquals(1, entries.size());

        localize.putProvider(entry2.getKey(), TEST_PROVIDER);
        assertEquals(1, entries.size());
    }

    @Test void testAddProviderByBaseName() {
        Localize localize = Localize.of();

        // NOTE: these methods delegate to `addProvider(ResourceBundleProvider)`
        localize.addProvider(TEST_PROVIDER_NAME);
        localize.addProvider(TEST2_PROVIDER_NAME);
        assertEquals(2, localize.getProviderEntries().size());
    }

    @Test void testPutNullReturningProvider() {
        Localize localize = Localize.of();
        String key = "key";

        localize.putProvider(key, _unusedLocale -> null);
        assertEquals("[missing]", localize.getValue("missing"));
    }

    @Test void testReturnedProviderFromRemove() {
        Localize localize = Localize.of();

        assertNull(localize.removeProvider("key"));
        assertNull(localize.removeProvider("key4"));

        localize.putProvider("key", TEST_PROVIDER);
        assertSame(TEST_PROVIDER, localize.removeProvider("key"));
        localize.putProvider("key3", TEST_PROVIDER);
        assertSame(TEST_PROVIDER, localize.removeProvider("key3"));
    }

    @Test void testLocale() {
        Localize localize = Localize.of();
        assertEquals(Locale.getDefault(), localize.getLocale());

        localize.setLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, localize.getLocale());

        localize.setLocale(Locale.CHINESE);
        assertEquals(Locale.CHINESE, localize.getLocale());

        localize.setLocale(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, localize.getLocale());
    }

    @Test void testGetValue() {
        Localize localize = getEmptyLocalizeInstance();

        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));
        assertEquals("", localize.getValue(() -> TEST_KEY_TEST));

        localize.putProvider("key", TEST_PROVIDER);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
        assertEquals("test", localize.getValue(() -> TEST_KEY_TEST));
        assertEquals("", localize.getValue("missing1"));

        localize.setLocale(Locale.CHINESE);
        assertEquals("早上好", localize.getValue(TEST_KEY_GREET));
        assertEquals("测试", localize.getValue(() -> TEST_KEY_TEST));
        assertEquals("", localize.getValue(() -> "missing2"));

        localize.setLocale(Locale.JAPANESE);
        assertEquals("おはよう", localize.getValue(TEST_KEY_GREET));
        assertEquals("テスト", localize.getValue(() -> TEST_KEY_TEST));
    }

    @Test void testExceptions() {
        assertThrows(NullPointerException.class, () -> Localize.of((Locale) null));
        assertThrows(NullPointerException.class, () -> Localize.of(Locale.ENGLISH, null));
        assertThrows(NullPointerException.class, () -> Localize.of(null, new LocalizeConfig()));

        Localize localize = Localize.of();
        assertThrows(NullPointerException.class, () -> localize.setLocale(null));
        assertThrows(NullPointerException.class, () -> localize.setFormatter(null));
    }

    @Test void testContainsProvider() {
        Localize localize = Localize.of();
        Function<String, Boolean> contains = localize::containsProvider;

        assertFalse(contains.apply("key"));

        localize.putProvider("key", TEST_PROVIDER);
        assertFalse(contains.apply("Key"));
        assertFalse(contains.apply("other"));
        assertTrue(contains.apply("key"));
        assertThrows(NullPointerException.class, () -> contains.apply(null));

        localize.removeProvider("key");
        assertFalse(contains.apply("key"));
    }

    @Test void testReplaceProvider() {
        Localize localize = getEmptyLocalizeInstance(Locale.JAPANESE);

        localize.putProvider("provider", TEST_PROVIDER);
        assertEquals("おはよう", localize.getValue(TEST_KEY_GREET));

        localize.putProvider("provider", TEST2_PROVIDER);
        assertEquals("おはようございます", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveProvider() {
        Localize localize = getEmptyLocalizeInstance(Locale.CHINESE);

        localize.putProvider("provider", TEST_PROVIDER);
        assertEquals("早上好", localize.getValue(TEST_KEY_GREET));

        localize.removeProvider("provider");
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveProviderMissingKey() {
        Localize localize = getEmptyLocalizeInstance();

        localize.putProvider("provider", TEST_PROVIDER);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));

        localize.removeProvider("");
        localize.removeProvider("Provider");
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testClearProviders() {
        Localize localize = getEmptyLocalizeInstance();
        Collection<Localize.ProviderEntry> entries = localize.getProviderEntries();

        assertFalse(localize.clearProviders());

        localize.addProvider(TEST_PROVIDER);

        assertFalse(entries.isEmpty());
        assertTrue(localize.clearProviders());
        assertTrue(entries.isEmpty());
        assertFalse(localize.clearProviders());
    }

    @Test void testRefreshNoProviders() {
        Localize localize = getEmptyLocalizeInstance();

        localize.refreshProviders();
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.refreshProvider("provider");
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testProviderRefresh() {
        Localize localize = getEmptyLocalizeInstance();
        Supplier<String> supplier = () -> localize.getValue(TEST_KEY_GREET);

        assertEquals("", supplier.get());

        localize.putProvider("provider", TEST_PROVIDER);
        assertEquals("hi", supplier.get());

        localize.refreshProviders();
        assertEquals("hi", supplier.get());

        localize.refreshProvider("provider");
        assertEquals("hi", supplier.get());

        localize.refreshProvider("nonexistent");
        assertEquals("hi", supplier.get());
    }

    @Test void testGetResourceBundles() {
        Localize localize = getEmptyLocalizeInstance();

        Collection<ResourceBundle> snapshotA = localize.getResourceBundles();
        assertTrue(snapshotA.isEmpty());

        localize.putProvider("key", TEST_PROVIDER);
        assertTrue(snapshotA.isEmpty());

        Collection<ResourceBundle> snapshotB = localize.getResourceBundles();
        assertEquals(1, snapshotB.size());
        assertNotEquals(snapshotA, snapshotB);

        localize.addProvider(TEST_PROVIDER);
        localize.addProvider(TEST2_PROVIDER);
        Collection<ResourceBundle> snapshotC = localize.getResourceBundles();

        assertTrue(snapshotA.isEmpty());
        assertEquals(1, snapshotB.size());
        assertEquals(3, snapshotC.size());
    }

    @Test void testRefreshUpdatesBundle() {
        Function<String, ResourceBundle> bundleFactory = value -> new ListResourceBundle() {
            @Override protected Object[][] getContents() {
                return new Object[][] {{ TEST_KEY_TEST, value }};
            }
        };

        AtomicReference<ResourceBundle> currentBundle = new AtomicReference<>(bundleFactory.apply("abc"));
        Localize localize = getEmptyLocalizeInstance();

        localize.putProvider("provider", locale -> currentBundle.get());
        assertEquals("abc", localize.getValue(TEST_KEY_TEST));

        // Realistically, this would be the file changing on disk or similar
        currentBundle.set(bundleFactory.apply("xyz"));
        assertEquals("abc", localize.getValue(TEST_KEY_TEST)); // still stale

        localize.refreshProvider("provider");
        assertEquals("xyz", localize.getValue(TEST_KEY_TEST));
    }

    @Test void setLocaleRefreshesUncoveredProvider() {
        Localize localize = Localize.of(Locale.ENGLISH);

        // Use a provider that will pause mid-fetch within `setLocale`
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.CHINESE);
        localize.putProvider("pauseSetLocale", pauser);

        var setLocaleTask = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.CHINESE));
        pauser.awaitPaused();

        var insertedDuringSetLocale = new ConcurrentProviderProbe(TEST_PROVIDER);
        // This method completes while `setLocale` is paused
        localize.putProvider("Z", insertedDuringSetLocale);

        pauser.resume();
        setLocaleTask.join();

        assertEquals(Locale.CHINESE, localize.getLocale());
        assertEquals("早上好", localize.getProviderEntry("Z").getBundle().getString(TEST_KEY_GREET));
        assertEquals(1, insertedDuringSetLocale.calls(Locale.CHINESE));
    }

    @Test void setLocaleDuringInsertFetch_forcesRefetch() {
        Localize localize = Localize.of(Locale.ENGLISH);
        // Use a provider that will pause mid-fetch within `putProvider`
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.ENGLISH);

        var insertTask = ConcurrentTestHelper.background(() -> localize.putProvider("Z", pauser));
        pauser.awaitPaused();

        // Change the locale while `putProvider` is paused
        localize.setLocale(Locale.CHINESE);
        pauser.resume();
        insertTask.join();

        assertEquals("早上好", localize.getProviderEntry("Z").getBundle().getString(TEST_KEY_GREET));
        assertEquals(1, pauser.calls(Locale.ENGLISH));
        assertEquals(1, pauser.calls(Locale.CHINESE));
    }

    @Test void setLocaleFailedNewCallDoesNotSupersedeOldOne() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var oldOne = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.JAPANESE);
        var newOne = new ConcurrentProviderProbe(TEST_PROVIDER).failOn(Locale.KOREAN);

        localize.putProvider("old", oldOne);
        localize.putProvider("new", newOne);

        // Pause `oldOne` mid-fetch within `setLocale`
        var older = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.JAPANESE));
        oldOne.awaitPaused();

        // While paused, switch locale to korean
        assertThrows(IllegalStateException.class, () -> localize.setLocale(Locale.KOREAN));

        oldOne.resume();
        older.join();

        // Locale remains Japanese since `newOne` threw an exception
        assertEquals(Locale.JAPANESE, localize.getLocale());
    }

    @Test void setLocaleOldCallIsSuperseded() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var provider = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.JAPANESE);
        localize.addProvider(provider);

        // Pause `provider` mid-fetch within `setLocale`
        var older = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.JAPANESE));
        provider.awaitPaused();

        localize.setLocale(Locale.KOREAN);
        provider.resume();
        older.join();

        // Locale becomes korean (newer call) despite japanese (older call) finishing after
        assertEquals(Locale.KOREAN, localize.getLocale());
    }

    @Test void raceBetweenSetLocaleAndPutProvider() {
        for (int i = 0; i < ConcurrentTestHelper.ITERATIONS; i++) {
            Localize localize = Localize.of(Locale.ENGLISH);

            ConcurrentTestHelper.race(
                () -> localize.setLocale(Locale.CHINESE),
                () -> localize.putProvider("key", new ConcurrentProviderProbe(TEST_PROVIDER))
            );

            Localize.ProviderEntry entry = localize.getProviderEntry("key");

            assertEquals(Locale.CHINESE, localize.getLocale());
            assertEquals("早上好", entry.getBundle().getString(TEST_KEY_GREET));
        }
    }
}
