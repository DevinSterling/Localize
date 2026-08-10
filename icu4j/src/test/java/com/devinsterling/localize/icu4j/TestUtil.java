package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.ResourceBundleProvider;

import java.util.Locale;
import java.util.ResourceBundle;

public final class TestUtil {
    public static final ResourceBundleProvider MESSAGE_FORMAT1 = locale -> ResourceBundle.getBundle("messageFormat1", locale);
    public static final ResourceBundleProvider MESSAGE_FORMAT2 = locale -> ResourceBundle.getBundle("messageFormat2", locale);
    public static final String TEST_KEY_CLICK_ME = "MyApp.clickMe";
    public static final String TEST_KEY_CLICK_LABEL = "MyApp.clickLabel";

    private TestUtil() {}

    public static Localize getMessageFormat1Instance() {
        return getLocalizeInstance(IcuFormatter.MESSAGE1_FORMATTER, MESSAGE_FORMAT1);
    }

    public static Localize getMessageFormat2Instance() {
        return getLocalizeInstance(IcuFormatter.MESSAGE2_FORMATTER, MESSAGE_FORMAT2);
    }

    private static Localize getLocalizeInstance(IcuFormatter formatter, ResourceBundleProvider provider) {
        Localize localize = Localize.of(Locale.ENGLISH);
        IcuProcessorConfig config = new IcuProcessorConfig();
        IcuProcessor processor = new IcuProcessor(config);

        config.setFormatter(formatter);
        localize.setProcessor(processor);
        localize.addBundleProvider(provider);
        return localize;
    }
}
