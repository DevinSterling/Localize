package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.ResourceBundleProvider;
import com.devinsterling.localize.fx.LocalizeFX;

import java.util.Locale;
import java.util.ResourceBundle;

public final class TestUtil {
    public static final ResourceBundleProvider TEST_PROVIDER = locale -> ResourceBundle.getBundle("test", locale);
    public static final ResourceBundleProvider TEST2_PROVIDER = locale -> ResourceBundle.getBundle("test2", locale);
    public static final String TEST_KEY_CLICK_ME = "MyApp.clickMe";
    public static final String TEST_KEY_CLICK_LABEL = "MyApp.clickLabel";

    private TestUtil() {}

    public static LocalizeFX getLocalizeFXInstance() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        localize.putBundleProvider("key", TEST_PROVIDER);
        return localize;
    }
}
