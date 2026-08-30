package com.devinsterling.localize.icu4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IcuPropertiesTest {

    @Test void testAbsentPropertiesFile() {
        IcuProperties properties = new IcuProperties("non/existent/file");

        // Ensure defaults
        assertEquals(IcuMessageFormat.MESSAGE2_FORMAT, properties.getMessageFormat());
    }

    @Test void testPresentPropertiesFile() {
        IcuProperties properties = new IcuProperties("localize.present.properties");

        // Ensure set properties
        assertEquals(IcuMessageFormat.MESSAGE1_FORMAT, properties.getMessageFormat());
    }

    @Test void testMalformedPropertiesFile() {
        // Should throw an error instead of ignoring any
        assertThrows(
            IllegalArgumentException.class,
            () -> new IcuProperties("localize.malformed.properties")
        );
    }
}
