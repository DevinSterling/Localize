package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.fx.LocalizeFX;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.fx.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeFXTest {

    @Test public void testLocaleProperty() {
        LocalizeFX localize = LocalizeFX.of();
        ObjectProperty<Locale> localeProperty = localize.localeProperty();

        assertEquals(Locale.getDefault(), localeProperty.get());

        localeProperty.set(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, localeProperty.get());

        assertThrows(NullPointerException.class, () -> localeProperty.set(null));

        assertEquals(Locale.JAPANESE, localeProperty.get());

        assertEquals(Locale.ENGLISH, LocalizeFX.of(Locale.ENGLISH).localeProperty().get());
    }

    @Test public void testMissingBundleAtFirst() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);

        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("", binding.get());

        localize.refresh();
        assertEquals("Click!", binding.get());

        StringBinding binding2 = localize.getBinding(() -> TEST_KEY_CLICK_ME);
        assertEquals(binding.get(), binding2.get());
    }

}
