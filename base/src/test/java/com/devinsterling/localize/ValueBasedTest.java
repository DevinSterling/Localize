package com.devinsterling.localize;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.jupiter.api.Test;

public class ValueBasedTest {

    @Test void testLocalizationRequestEquality() {
        EqualsVerifier.simple()
                      .forClass(LocalizationRequest.class)
                      .withNonnullFields("source", "arguments")
                      .verify();
    }

    @Test void testLocalizationFormatterRequestEquality() {
        EqualsVerifier.simple()
                      .forClass(LocalizationFormatter.Request.class)
                      .withNonnullFields("pattern", "arguments", "locale")
                      .verify();
    }
}
