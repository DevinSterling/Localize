package com.devinsterling.localize.test;

import com.devinsterling.localize.LocalizationRequestProcessor;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.devinsterling.localize.test.TestUtil.*;

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
        String key = "key";

        assertTrue(localize.getResourceBundles().isEmpty());
        assertTrue(localize.putBundleProvider(key, TEST_PROVIDER));
        assertFalse(localize.putBundleProvider(key, TEST_PROVIDER));
        assertEquals(1, localize.getResourceBundles().size());

        assertTrue(localize.removeBundleProvider(key));
        assertTrue(localize.getResourceBundles().isEmpty());

        assertTrue(localize.putBundleProvider(key, TEST_PROVIDER));
        assertTrue(localize.putBundleProvider("other", TEST_PROVIDER));
        assertEquals(2, localize.getResourceBundles().size());
    }

    @Test void testAddProvider() {
        Localize localize = Localize.of();

        String key1 = localize.addBundleProvider(TEST_PROVIDER);
        String key2 = localize.addBundleProvider(TEST2_PROVIDER);
        assertEquals(2, localize.getResourceBundles().size());

        localize.removeBundleProvider(key1);
        assertEquals(1, localize.getResourceBundles().size());

        localize.putBundleProvider(key2, TEST_PROVIDER);
        assertEquals(1, localize.getResourceBundles().size());
    }

    @Test void testPutNullReturningProvider() {
        Localize localize = Localize.of();
        String key = "key";

        localize.putBundleProvider(key, _unusedLocale -> null);
        assertEquals("", localize.getValue("missing"));
    }

    @Test void testRemoveProvider() {
        Localize localize = Localize.of();

        assertFalse(localize.removeBundleProvider("key"));
        assertFalse(localize.removeBundleProvider("key4"));

        localize.putBundleProvider("key", TEST_PROVIDER);
        assertTrue(localize.removeBundleProvider("key"));
        localize.putBundleProvider("key3", TEST_PROVIDER);
        assertTrue(localize.removeBundleProvider("key3"));
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
        Localize localize = Localize.of(Locale.ENGLISH);

        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));
        assertEquals("", localize.getValue(() -> TEST_KEY_TEST));

        localize.putBundleProvider("key", TEST_PROVIDER);
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

    @Test void testProcessor() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String sample = "sample";
        LocalizationRequestProcessor mock = (bundle, request) -> sample;

        assertEquals(Localize.DEFAULT_PROCESSOR, localize.getProcessor());

        localize.setProcessor(mock);
        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.putBundleProvider("key", TEST_PROVIDER);
        assertEquals(mock, localize.getProcessor());
        assertEquals(sample, localize.getValue(TEST_KEY_GREET));
        assertEquals(sample, localize.getValue(() -> TEST_KEY_TEST));

        localize.setProcessor(Localize.DEFAULT_PROCESSOR);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
        assertEquals("test", localize.getValue(() -> TEST_KEY_TEST));
    }

    @Test void testExceptions() {
        assertThrows(NullPointerException.class, () -> Localize.of((Locale) null));
        assertThrows(NullPointerException.class, () -> Localize.of(Locale.ENGLISH, null));
        assertThrows(NullPointerException.class, () -> Localize.of(null, new LocalizeConfig()));

        Localize localize = Localize.of();
        assertThrows(NullPointerException.class, () -> localize.setLocale(null));
        assertThrows(NullPointerException.class, () -> localize.setProcessor(null));
    }

    @Test void testReplaceBundleProvider() {
        Localize localize = Localize.of(Locale.JAPANESE);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("おはよう", localize.getValue(TEST_KEY_GREET));

        localize.putBundleProvider("provider", TEST2_PROVIDER);
        assertEquals("おはようございます", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveBundleProvider() {
        Localize localize = Localize.of(Locale.CHINESE);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("早上好", localize.getValue(TEST_KEY_GREET));

        localize.removeBundleProvider("provider");
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRemoveBundleProviderMissingKey() {
        Localize localize = Localize.of(Locale.ENGLISH);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));

        localize.removeBundleProvider("");
        localize.removeBundleProvider("Provider");
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRefreshNoProviders() {
        Localize localize = Localize.of(Locale.ENGLISH);

        localize.refresh();
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.refresh("provider");
        assertEquals("", localize.getValue(TEST_KEY_GREET));
    }

    @Test void testRefresh() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Supplier<String> supplier = () -> localize.getValue(TEST_KEY_GREET);

        assertEquals("", supplier.get());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("hi", supplier.get());

        localize.refresh();
        assertEquals("hi", supplier.get());

        localize.refresh("provider");
        assertEquals("hi", supplier.get());

        localize.refresh("nonexistent");
        assertEquals("hi", supplier.get());
    }

    @Test void testRefreshUpdatesBundle() {
        Function<String, ResourceBundle> bundleFactory = value -> new ListResourceBundle() {
            @Override protected Object[][] getContents() {
                return new Object[][] {{ TEST_KEY_TEST, value }};
            }
        };

        AtomicReference<ResourceBundle> currentBundle = new AtomicReference<>(bundleFactory.apply("abc"));
        Localize localize = Localize.of(Locale.ENGLISH);

        localize.putBundleProvider("provider", locale -> currentBundle.get());
        assertEquals("abc", localize.getValue(TEST_KEY_TEST));

        // Realistically, this would be the file changing on disk or similar
        currentBundle.set(bundleFactory.apply("xyz"));
        assertEquals("abc", localize.getValue(TEST_KEY_TEST)); // still stale

        localize.refresh("provider");
        assertEquals("xyz", localize.getValue(TEST_KEY_TEST));
    }
}
