package com.devinsterling.localize.icu4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IcuProcessorConfigTest {

    @Test void testDefaultFormatter() {
        IcuProcessorConfig config = new IcuProcessorConfig();

        assertEquals(IcuFormatter.MESSAGE2_FORMATTER, config.getFormatter());
    }

    @Test void testSetFormatter() {
        IcuProcessorConfig config = new IcuProcessorConfig();

        config.setFormatter(IcuFormatter.MESSAGE1_FORMATTER);
        assertEquals(IcuFormatter.MESSAGE1_FORMATTER, config.getFormatter());
    }

    @Test void testSetFormatterRejectNulls() {
        IcuProcessorConfig config = new IcuProcessorConfig();

        assertThrows(NullPointerException.class, () -> config.setFormatter(null));
    }

    @Test void testEqualityAndHashCode() {
        IcuProcessorConfig config1 = new IcuProcessorConfig();
        IcuProcessorConfig config2 = new IcuProcessorConfig();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());

        config1.setFormatter(IcuFormatter.MESSAGE1_FORMATTER);

        // - `config1` must be placed before null, else `IcuProcessorConfig#equal(null)` is never called here.
        // noinspection MisorderedAssertEqualsArguments
        assertNotEquals(config1, null);
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test void testFormatterSystemProperty() {
        String property = "localize.icu4j.formatter";

        System.setProperty(property, "java.text.MessageFormat");
        assertThrows(IllegalStateException.class, IcuProcessorConfig::new);

        System.setProperty(property, "com.ibm.icu.message2.MessageFormatter");
        IcuProcessorConfig configA = new IcuProcessorConfig();

        System.setProperty(property, "com.ibm.icu.text.MessageFormat");
        IcuProcessorConfig configB = new IcuProcessorConfig();

        System.clearProperty(property);
        // `com.ibm.icu.message2.MessageFormatter` by default
        IcuProcessorConfig configC = new IcuProcessorConfig();

        assertEquals(IcuFormatter.MESSAGE2_FORMATTER, configA.getFormatter());
        assertEquals(IcuFormatter.MESSAGE1_FORMATTER, configB.getFormatter());
        assertEquals(IcuFormatter.MESSAGE2_FORMATTER, configC.getFormatter());
    }
}
