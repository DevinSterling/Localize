package com.devinsterling.localize.icu4j;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IcuFormatterConfigTest {

    @Test void testDefaultFormatter() {
        IcuFormatterConfig config = new IcuFormatterConfig();

        assertEquals(IcuMessageFormat.MESSAGE2_FORMAT, config.getMessageFormat());
    }

    @Test void testSetFormatter() {
        IcuFormatterConfig config = new IcuFormatterConfig();

        config.setFormatter(IcuMessageFormat.MESSAGE1_FORMAT);
        assertEquals(IcuMessageFormat.MESSAGE1_FORMAT, config.getMessageFormat());
    }

    @Test void testSetFormatterRejectNulls() {
        IcuFormatterConfig config = new IcuFormatterConfig();

        assertThrows(NullPointerException.class, () -> config.setFormatter(null));
    }

    @Test void testIcuFormatterConfigEquality() {
        EqualsVerifier.simple()
                      .forClass(IcuFormatterConfig.class)
                      .withNonnullFields("messageFormat")
                      .verify();
    }

    @Test void testFormatterSystemProperty() {
        String property = "localize.icu4j.formatter";

        System.setProperty(property, "java.text.MessageFormat");
        assertThrows(IllegalStateException.class, IcuFormatterConfig::new);

        System.setProperty(property, "com.ibm.icu.message2.MessageFormatter");
        IcuFormatterConfig configA = new IcuFormatterConfig();

        System.setProperty(property, "com.ibm.icu.text.MessageFormat");
        IcuFormatterConfig configB = new IcuFormatterConfig();

        System.clearProperty(property);
        // `com.ibm.icu.message2.MessageFormatter` by default
        IcuFormatterConfig configC = new IcuFormatterConfig();

        assertEquals(IcuMessageFormat.MESSAGE2_FORMAT, configA.getMessageFormat());
        assertEquals(IcuMessageFormat.MESSAGE1_FORMAT, configB.getMessageFormat());
        assertEquals(IcuMessageFormat.MESSAGE2_FORMAT, configC.getMessageFormat());
    }
}
