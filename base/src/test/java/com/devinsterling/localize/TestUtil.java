package com.devinsterling.localize;

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

    private TestUtil() {}

    public static Localize getLocalizeInstance() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.addBundleProvider(TEST_PROVIDER);
        return localize;
    }
}
