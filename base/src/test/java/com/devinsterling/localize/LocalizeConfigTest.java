package com.devinsterling.localize;

import com.devinsterling.localize.event.LocalizeEvent;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizeConfigTest {

    @Test public void testLocalizeConfigEquality() {
        EqualsVerifier.simple().forClass(LocalizeConfig.class).verify();
    }

    @Test void testDefaultMissingValue() {
        LocalizeConfig config = new LocalizeConfig();
        Localize localize = Localize.of(Locale.ENGLISH, config);
        String missingValue = "Missing Value";

        // Default values
        assertEquals(config, localize.getConfig());

        config.setDefaultMissingValue(missingValue);
        assertEquals(missingValue, localize.getValue("Missing Key"));
    }

    @Test void testMissingResourceBundle() {
        Localize localize = Localize.of();
        LocalizeConfig config = localize.getConfig();
        ResourceBundleProvider provider = l -> ResourceBundle.getBundle("missing", l);

        // Resource bundles
        assertThrows(MissingResourceException.class, () -> localize.putProvider("key", provider));

        config.setIgnoreProviderExceptions(true);
        assertDoesNotThrow(() -> localize.putProvider("key", provider));
    }

    @Test void testValueNotFound() {
        Localize localize = Localize.of();
        LocalizeConfig config = localize.getConfig();

        // Value not found
        assertDoesNotThrow(() -> localize.getValue("Missing Key"));

        config.setThrowWhenNoValueFound(true);
        assertThrows(MissingResourceException.class, () -> localize.getValue("Missing Key"));
    }

    @Test void testIgnoreFormatterExceptions() {
        Localize localize = getLocalizeInstance();
        LocalizeConfig config = localize.getConfig();

        localize.setFormatter(request -> {
            throw new TestException();
        });

        assertThrows(TestException.class, () -> localize.getValue(TEST_KEY_GREET));

        config.setIgnoreFormatterExceptions(true);
        assertDoesNotThrow(() -> localize.getValue(TEST_KEY_GREET));
    }

    @Test void testIgnoreProviderExceptions() {
        Localize localize = getLocalizeInstance();
        LocalizeConfig config = localize.getConfig();

        ResourceBundleProvider exceptionProvider = locale -> {
            throw new TestException();
        };

        assertThrows(TestException.class, () -> localize.addProvider(exceptionProvider));
        assertThrows(TestException.class, localize::refreshProviders);

        config.setIgnoreProviderExceptions(true);
        assertDoesNotThrow(() -> localize.addProvider(exceptionProvider));
        assertDoesNotThrow(localize::refreshProviders);
    }

    @Test void testIgnoreEventListenerExceptions() {
        Localize localize = getLocalizeInstance();
        LocalizeConfig config = localize.getConfig();

        localize.addListener(LocalizeEvent.class, event -> {
            throw new TestException();
        });

        assertThrows(TestException.class, () -> localize.addProvider(TEST_PROVIDER));

        config.setIgnoreListenerExceptions(true);
        assertDoesNotThrow(() -> localize.addProvider(TEST_PROVIDER));
    }

    private static class TestException extends RuntimeException {}
}
