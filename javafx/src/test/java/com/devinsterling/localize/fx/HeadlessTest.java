package com.devinsterling.localize.fx;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class HeadlessTest {
    @Test void uninitializedJavaFXDoesNotCrash() {
        LocalizeFX fx = LocalizeFX.of();

        assertDoesNotThrow(() -> fx.addProvider(locale -> null));
        assertDoesNotThrow(() -> fx.setLocale(Locale.JAPANESE));
    }
}
