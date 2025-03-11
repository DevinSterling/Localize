package com.devinsterling.localize.test;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.ResourceBundleProvider;

import java.util.Locale;
import java.util.ResourceBundle;

public final class TestUtil {
    public static final String BASE_NAME = "test";
    public static final ResourceBundleProvider TEST_PROVIDER = locale -> ResourceBundle.getBundle(BASE_NAME, locale);

    public static final String TEST_KEY_GREET = "Test.greet";
    public static final String TEST_KEY_TEST = "Test.test";
    public static final String TEST_KEY_NAMED = "Test.named";
    public static final String TEST_KEY_NUMBERED = "Test.numbered";

    private TestUtil() {}

    public static Localize getLocalizeInstance() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.putBundleProvider("key", TEST_PROVIDER);
        return localize;
    }
}
