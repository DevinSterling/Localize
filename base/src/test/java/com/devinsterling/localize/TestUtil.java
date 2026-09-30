package com.devinsterling.localize;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ResourceBundle;

public final class TestUtil {
    public static final String TEST_PROVIDER_NAME = "test";
    public static final String TEST2_PROVIDER_NAME = "test2";

    public static final ResourceBundleProvider TEST_PROVIDER = locale -> ResourceBundle.getBundle(TEST_PROVIDER_NAME, locale);
    public static final ResourceBundleProvider TEST2_PROVIDER = locale -> ResourceBundle.getBundle(TEST2_PROVIDER_NAME, locale);

    public static final String TEST_KEY_GREET = "Test.greet";
    public static final String TEST_KEY_TEST = "Test.test";
    public static final String TEST_KEY_NAMED = "Test.named";
    public static final String TEST_KEY_NUMBERED = "Test.numbered";
    public static final String TEST_KEY_OUTPUT = "Test.output";
    private static final String DEFAULT_VALUE = "";

    private TestUtil() {}

    /// Returns an instance with:
    /// - [Locale#ENGLISH] set as the locale.
    /// - [DEFAULT_VALUE] set as the default value.
    /// - [TEST_PROVIDER] present.
    public static Localize getLocalizeInstance() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.addProvider(TEST_PROVIDER);
        return localize;
    }

    /// Returns an instance with:
    /// - [Locale#ENGLISH] set as the locale.
    /// - [DEFAULT_VALUE] set as the default value.
    public static Localize getEmptyLocalizeInstance() {
        return getEmptyLocalizeInstance(Locale.ENGLISH);
    }

    /// Returns an instance with:
    /// - [DEFAULT_VALUE] set as the default value.
    public static Localize getEmptyLocalizeInstance(Locale locale) {
        Localize localize = Localize.of(locale);
        localize.getConfig().setDefaultMissingValue(DEFAULT_VALUE);
        return localize;
    }

    public static void awaitGarbageCollection() {
        Object object = new Object();
        WeakReference<Object> reference = new WeakReference<>(object);
        object = null;

        while(reference.get() != null) {
            System.gc();
        }
    }
}
