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

    private TestUtil() {}

    public static LocalizeSwing getLocalizeSwingInstance() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
        localize.addProvider(TEST_PROVIDER);
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
