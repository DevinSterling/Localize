package com.devinsterling.localize;

import com.devinsterling.localize.concurrent.ConcurrentProviderProbe;
import com.devinsterling.localize.concurrent.ConcurrentTestHelper;
import com.devinsterling.localize.event.ProviderEvent;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizeTest {
    private final Localize.ProviderKey providerKey = Localize.ProviderKey.of("key");
    private final Localize.ProviderKey otherKey = Localize.ProviderKey.of("Key");
    private final Localize.ProviderKey missingKey = Localize.ProviderKey.of("missing");

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

        assertTrue(entries.isEmpty());
        assertNull(localize.putProvider(providerKey, TEST_PROVIDER));
        assertSame(TEST_PROVIDER, localize.putProvider(providerKey, TEST2_PROVIDER));
        assertEquals(1, entries.size());

        assertSame(TEST2_PROVIDER, localize.removeProvider(providerKey));
        assertTrue(entries.isEmpty());

        assertNull(localize.putProvider(providerKey, TEST_PROVIDER));
        assertNull(localize.putProvider(otherKey, TEST_PROVIDER));
        assertEquals(2, entries.size());
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

    @Test void testPutNullReturningProvider() {
        Localize localize = Localize.of();

        localize.putProvider(providerKey, _unusedLocale -> null);
        assertEquals("[missing]", localize.getValue("missing"));
    }

    @Test void testReturnedProviderFromRemove() {
        Localize localize = Localize.of();

        assertNull(localize.removeProvider(providerKey));
        assertNull(localize.removeProvider(otherKey));

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertSame(TEST_PROVIDER, localize.removeProvider(providerKey));
        localize.putProvider(otherKey, TEST_PROVIDER);
        assertSame(TEST_PROVIDER, localize.removeProvider(otherKey));
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

        // No providers contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));
        assertEquals("", localize.getValue(() -> TEST_KEY_TEST));

        localize.putProvider(providerKey, TEST_PROVIDER);
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
        Function<Localize.ProviderKey, Boolean> contains = localize::containsProvider;

        assertFalse(contains.apply(providerKey));

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertFalse(contains.apply(otherKey));
        assertFalse(contains.apply(missingKey));
        assertTrue(contains.apply(providerKey));
        assertThrows(NullPointerException.class, () -> contains.apply(null));

        localize.removeProvider(providerKey);
        assertFalse(contains.apply(providerKey));
    }

    @Test void testReplaceProvider() {
        Localize localize = getEmptyLocalizeInstance(Locale.JAPANESE);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("おはよう", localize.getValue(TEST_KEY_GREET));

        localize.putProvider(providerKey, TEST2_PROVIDER);
        assertEquals("おはようございます", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveProvider() {
        Localize localize = getEmptyLocalizeInstance(Locale.CHINESE);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("早上好", localize.getValue(TEST_KEY_GREET));

        localize.removeProvider(providerKey);
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveProviderMissingKey() {
        Localize localize = getEmptyLocalizeInstance();

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));

        localize.removeProvider(missingKey);
        localize.removeProvider(otherKey);
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

        localize.refreshProvider(providerKey);
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testProviderRefresh() {
        Localize localize = getEmptyLocalizeInstance();
        Supplier<String> supplier = () -> localize.getValue(TEST_KEY_GREET);

        assertEquals("", supplier.get());

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("hi", supplier.get());

        localize.refreshProviders();
        assertEquals("hi", supplier.get());

        localize.refreshProvider(providerKey);
        assertEquals("hi", supplier.get());

        localize.refreshProvider(missingKey);
        assertEquals("hi", supplier.get());
    }

    @Test void testGetResourceBundles() {
        Localize localize = getEmptyLocalizeInstance();

        Collection<PatternSource> snapshotA = localize.getPatternSources();
        assertTrue(snapshotA.isEmpty());

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertTrue(snapshotA.isEmpty());

        Collection<PatternSource> snapshotB = localize.getPatternSources();
        assertEquals(1, snapshotB.size());
        assertNotEquals(snapshotA, snapshotB);

        localize.addProvider(TEST_PROVIDER);
        localize.addProvider(TEST2_PROVIDER);
        Collection<PatternSource> snapshotC = localize.getPatternSources();

        assertTrue(snapshotA.isEmpty());
        assertEquals(1, snapshotB.size());
        assertEquals(3, snapshotC.size());
    }

    @Test void testRefreshUpdatesBundle() {
        Function<String, PatternSource> sourceFactory = value -> key -> value;

        AtomicReference<PatternSource> currentSource = new AtomicReference<>(sourceFactory.apply("abc"));
        Localize localize = getEmptyLocalizeInstance();

        localize.putProvider(providerKey, locale -> currentSource.get());
        assertEquals("abc", localize.getValue(TEST_KEY_TEST));

        // Realistically, this would be the file changing on disk or similar
        currentSource.set(sourceFactory.apply("xyz"));
        assertEquals("abc", localize.getValue(TEST_KEY_TEST)); // still stale

        localize.refreshProvider(providerKey);
        assertEquals("xyz", localize.getValue(TEST_KEY_TEST));
    }

    @Test void setLocaleRefreshesUncoveredProvider() {
        Localize localize = Localize.of(Locale.ENGLISH);

        // Use a provider that will pause mid-fetch within `setLocale`
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.CHINESE);
        localize.addProvider(pauser);

        var setLocaleTask = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.CHINESE));
        pauser.awaitPaused();

        var insertedDuringSetLocale = new ConcurrentProviderProbe(TEST_PROVIDER);
        // This method completes while `setLocale` is paused
        localize.putProvider(providerKey, insertedDuringSetLocale);

        pauser.resume();
        setLocaleTask.join();

        assertEquals(Locale.CHINESE, localize.getLocale());
        assertEquals("早上好", localize.getProviderEntry(providerKey).getPatternSource().getPattern(TEST_KEY_GREET));
        assertEquals(1, insertedDuringSetLocale.calls(Locale.CHINESE));
    }

    @Test void setLocaleDuringInsertForcesRefetch() {
        Localize localize = Localize.of(Locale.ENGLISH);
        // Provider that pauses mid-fetch within `putProvider`
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.ENGLISH);

        var insertTask = ConcurrentTestHelper.background(() -> localize.putProvider(providerKey, pauser));
        pauser.awaitPaused();

        // Change the locale while `putProvider` is paused
        localize.setLocale(Locale.CHINESE);
        pauser.resume();
        insertTask.join();

        assertEquals("早上好", localize.getProviderEntry(providerKey).getPatternSource().getPattern(TEST_KEY_GREET));
        assertEquals(1, pauser.calls(Locale.ENGLISH));
        assertEquals(1, pauser.calls(Locale.CHINESE));
    }

    @Test void setLocaleFailedNewCallDoesNotSupersedeOldOne() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var oldOne = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.JAPANESE);
        var newOne = new ConcurrentProviderProbe(TEST_PROVIDER).failOn(Locale.KOREAN);

        var entry = localize.addProvider(oldOne);
        localize.addProvider(newOne);

        // Pause `oldOne` mid-fetch within `setLocale`
        var older = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.JAPANESE));
        oldOne.awaitPaused();

        // While paused, switch locale to korean
        assertThrows(IllegalStateException.class, () -> localize.setLocale(Locale.KOREAN));

        oldOne.resume();
        older.join();

        // Locale remains Japanese since `newOne` threw an exception
        assertEquals(Locale.JAPANESE, localize.getLocale());
        assertEquals("おはよう", entry.getPatternSource().getPattern(TEST_KEY_GREET));
    }

    @Test void setLocaleOldCallIsSuperseded() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var provider = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.JAPANESE);
        var entry = localize.addProvider(provider);

        // Pause `provider` mid-fetch within `setLocale`
        var older = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.JAPANESE));
        provider.awaitPaused();

        localize.setLocale(Locale.CHINESE);
        provider.resume();
        older.join();

        // Locale becomes korean (newer call) despite japanese (older call) finishing after
        assertEquals(Locale.CHINESE, localize.getLocale());
        assertEquals("早上好", entry.getPatternSource().getPattern(TEST_KEY_GREET));
    }

    @Test void setLocaleOldCallIsSupersededABA() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.JAPANESE);
        var entry = localize.addProvider(pauser);

        var slowTask = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.JAPANESE));
        pauser.awaitPaused();

        // Change back to english
        localize.setLocale(Locale.ENGLISH);
        pauser.resume();
        slowTask.join();

        assertEquals(Locale.ENGLISH, localize.getLocale());
        assertEquals("hi",entry.getPatternSource().getPattern(TEST_KEY_GREET));
    }

    @Test void raceBetweenSetLocaleAndPutProvider() {
        for (int i = 0; i < ConcurrentTestHelper.ITERATIONS; i++) {
            Localize localize = Localize.of(Locale.ENGLISH);

            ConcurrentTestHelper.race(
                () -> localize.setLocale(Locale.CHINESE),
                () -> localize.putProvider(providerKey, new ConcurrentProviderProbe(TEST_PROVIDER))
            );

            Localize.ProviderEntry entry = localize.getProviderEntry(providerKey);

            assertEquals(Locale.CHINESE, localize.getLocale());
            assertEquals("早上好", entry.getPatternSource().getPattern(TEST_KEY_GREET));
        }
    }

    @Test void bulkRefreshSupersededByLocaleChange() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER);
        var entry = localize.addProvider(pauser);

        pauser.pauseOn(Locale.ENGLISH);
        var refreshTask = ConcurrentTestHelper.background(localize::refreshProviders);
        pauser.awaitPaused();

        localize.setLocale(Locale.CHINESE);
        pauser.resume();
        boolean performedBulkRefresh = refreshTask.join();

        // Despite the refresh task finishing after, the setLocale call has higher precedence
        assertFalse(performedBulkRefresh);
        assertEquals(Locale.CHINESE, localize.getLocale());
        assertEquals("早上好", entry.getPatternSource().getPattern(TEST_KEY_GREET));
    }

    @Test void individualRefreshSupersededByBulkRefresh() {
        Localize localize = Localize.of(Locale.ENGLISH);
        AtomicReference<String> output = new AtomicReference<>();
        var pauser = new ConcurrentProviderProbe(locale -> {
            String value = output.get();
            return s -> value;
        });
        var entry = localize.addProvider(pauser);

        pauser.pauseOn(Locale.ENGLISH);
        var individualTask = ConcurrentTestHelper.background(() -> localize.refreshProvider(entry.getKey()));
        pauser.awaitPaused();

        output.set("bulk");
        localize.refreshProviders();

        output.set("individual");
        pauser.resume();
        boolean performedIndividualRefresh = individualTask.join();

        // Despite the individual refresh finishing later, the bulk refresh takes precedence
        assertFalse(performedIndividualRefresh);
        assertEquals("bulk", entry.getPatternSource().getPattern(TEST_KEY_TEST));
    }

    @Test void setLocaleSupersedesNewerRefreshForOldLocale() {
        Localize localize = Localize.of(Locale.ENGLISH);
        var pauser = new ConcurrentProviderProbe(TEST_PROVIDER).pauseOn(Locale.CHINESE);
        var entry = localize.addProvider(pauser);

        var setLocaleTask = ConcurrentTestHelper.background(() -> localize.setLocale(Locale.CHINESE));
        pauser.awaitPaused();

        entry.refresh();
        pauser.resume();
        setLocaleTask.join();

        // A newer locale version takes precedence over plain ticket value ordering.
        assertEquals(Locale.CHINESE, localize.getLocale());
        assertEquals("早上好", entry.getPatternSource().getPattern(TEST_KEY_GREET));
    }

    @Test void refreshProvidersAggregatesProviderAndListenerExceptions() {
        Localize localize = Localize.of(Locale.ENGLISH);

        var providerException = new IllegalStateException("Provider failed");
        var listenerException = new IllegalArgumentException("Listener failed");
        var count = new AtomicInteger();

        var faultyProvider = new ConcurrentProviderProbe(TEST_PROVIDER).throwOnFail(() -> providerException);

        localize.addProvider(faultyProvider);
        localize.addProvider(locale -> {
            count.incrementAndGet();
            return TEST_PROVIDER.provide(locale);
        });
        localize.addListener(ProviderEvent.Refreshed.class, event -> {
            throw listenerException;
        });
        localize.addListener(ProviderEvent.Refreshed.class, event -> count.incrementAndGet());

        faultyProvider.failOn(Locale.ENGLISH);
        var caught = assertThrows(IllegalStateException.class, localize::refreshProviders);

        // `+ 1` because calling `addProvider` invokes the provider once
        assertEquals(3, count.get());
        assertSame(caught, providerException);
        assertArrayEquals(new Throwable[] { listenerException }, caught.getSuppressed());
    }
}
