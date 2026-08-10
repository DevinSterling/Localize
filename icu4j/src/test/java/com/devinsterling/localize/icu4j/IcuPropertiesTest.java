package com.devinsterling.localize.icu4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IcuPropertiesTest {

    @Test void testAbsentPropertiesFile() {
        IcuProperties properties = new IcuProperties("non/existent/file");

        // Ensure defaults
        assertEquals(IcuFormatter.MESSAGE2_FORMATTER, properties.getFormatterType());
    }

    @Test void testPresentPropertiesFile() {
        IcuProperties properties = new IcuProperties("localize.present.properties");

        // Ensure set properties
        assertEquals(IcuFormatter.MESSAGE1_FORMATTER, properties.getFormatterType());
    }

    @Test void testMalformedPropertiesFile() {
        // Should throw an error instead of ignoring any
        assertThrows(
            IllegalArgumentException.class,
            () -> new IcuProperties("localize.malformed.properties")
        );
    }
}
