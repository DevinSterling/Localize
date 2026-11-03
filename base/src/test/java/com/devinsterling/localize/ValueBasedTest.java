package com.devinsterling.localize;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.jupiter.api.Test;

import java.util.Locale;

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
                      .withNonnullFields("localize", "pattern", "arguments", "locale")
                      .withPrefabValues(Localize.class, Localize.of(Locale.ENGLISH), Localize.of(Locale.JAPANESE))
                      .verify();
    }
}
