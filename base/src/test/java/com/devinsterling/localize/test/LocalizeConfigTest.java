package com.devinsterling.localize.test;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.ResourceBundleProvider;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.devinsterling.localize.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeConfigTest {

    @Test public void testDefaultMissingValue() {
        LocalizeConfig config = new LocalizeConfig();
        Localize localize = Localize.of(Locale.ENGLISH, config);
        String missingValue = "Missing Value";

        // Default values
        assertEquals(config, localize.getConfig());
        config.setDefaultMissingValue(missingValue);
        assertEquals(missingValue, localize.getValue("Missing Key"));
    }

    @Test public void testMissingResourceBundle() {
        Localize localize = Localize.of();
        LocalizeConfig config = localize.getConfig();
        ResourceBundleProvider provider = l -> ResourceBundle.getBundle("missing", l);

        // Resource bundles
        assertThrows(
                MissingResourceException.class,
                () -> localize.putBundleProvider("key", provider));
        config.setIgnoreMissingResourceBundles(true);
        assertDoesNotThrow(
                () -> localize.putBundleProvider("key", provider));

    }

    @Test public void testValueNotFound() {
        Localize localize = Localize.of();
        LocalizeConfig config = localize.getConfig();

        // Value not found
        assertDoesNotThrow(
                () -> localize.getValue("Missing Key"));
        config.setThrowWhenNoValueFound(true);
        assertThrows(
                MissingResourceException.class,
                () -> localize.getValue("Missing Key"));
    }

    @Test public void testIgnoreProcessingExceptions() {
        Localize localize = getLocalizeInstance();
        LocalizeConfig config = localize.getConfig();

        localize.setProcessor((bundle, request) -> {
            throw new TestException();
        });

        assertThrows(TestException.class, () -> localize.getValue(TEST_KEY_GREET));
        config.setIgnoreProcessingExceptions(true);
        assertDoesNotThrow(() -> localize.getValue(TEST_KEY_GREET));
    }

    private static class TestException extends RuntimeException {}
}
