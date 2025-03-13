package com.devinsterling.localize.test;

import com.devinsterling.localize.LocalizationRequestProcessor;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeTest {

    @Test public void TestPutProvider() {
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

    @Test public void testRemoveProvider() {
        Localize localize = Localize.of();

        assertFalse(localize.removeBundleProvider("key"));
        assertFalse(localize.removeBundleProvider("key4"));

        localize.putBundleProvider("key", TEST_PROVIDER);
        assertTrue(localize.removeBundleProvider("key"));
        localize.putBundleProvider("key3", TEST_PROVIDER);
        assertTrue(localize.removeBundleProvider("key3"));
    }

    @Test public void testLocale() {
        Localize localize = Localize.of();
        assertEquals(Locale.getDefault(), localize.getLocale());

        localize.setLocale(Locale.ENGLISH);
        assertEquals(localize.getLocale(), Locale.ENGLISH);

        localize.setLocale(Locale.CHINESE);
        assertEquals(localize.getLocale(), Locale.CHINESE);

        localize.setLocale(Locale.JAPANESE);
        assertEquals(localize.getLocale(), Locale.JAPANESE);
    }

    @Test public void testGetValue() {
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

    @Test public void testProcessor() {
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

    @Test public void testExceptions() {
        assertThrows(NullPointerException.class, () -> Localize.of(null));
        assertThrows(NullPointerException.class, () -> Localize.of(Locale.ENGLISH, null));
        assertThrows(NullPointerException.class, () -> Localize.of(null, new LocalizeConfig()));

        Localize localize = Localize.of();
        assertThrows(NullPointerException.class, () -> localize.setLocale(null));
        assertThrows(NullPointerException.class, () -> localize.setProcessor(null));
    }
}
