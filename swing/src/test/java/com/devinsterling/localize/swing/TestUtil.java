package com.devinsterling.localize.swing;

import com.devinsterling.localize.ResourceBundleProvider;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ResourceBundle;

public final class TestUtil {
    public static final ResourceBundleProvider TEST_PROVIDER = locale -> ResourceBundle.getBundle("test", locale);
    public static final ResourceBundleProvider TEST2_PROVIDER = locale -> ResourceBundle.getBundle("test2", locale);
    public static final String TEST_KEY_CLICK_ME = "MyApp.clickMe";
    public static final String TEST_KEY_CLICK_LABEL = "MyApp.clickLabel";
    public static final String TEST_KEY_PRINT = "MyApp.print";
    private static final String DEFAULT_VALUE = "";

    private TestUtil() {}

    /// Returns an instance with:
    /// - [Locale#ENGLISH] set as the locale.
    /// - [DEFAULT_VALUE] set as the default value.
    /// - [TEST_PROVIDER] present.
    public static LocalizeSwing getLocalizeSwingInstance() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();
        localize.addProvider(TEST_PROVIDER);
        return localize;
    }

    /// Returns an instance with:
    /// - [Locale#ENGLISH] set as the locale.
    /// - [DEFAULT_VALUE] set as the default value.
    public static LocalizeSwing getEmptyLocalizeSwingInstance() {
        return getEmptyLocalizeSwingInstance(Locale.ENGLISH);
    }

    /// Returns an instance with:
    /// - [DEFAULT_VALUE] set as the default value.
    public static LocalizeSwing getEmptyLocalizeSwingInstance(Locale locale) {
        LocalizeSwing localize = LocalizeSwing.of(locale);
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

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
