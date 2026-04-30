package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.fx.LocalizeFX;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.devinsterling.localize.fx.test.TestUtil.*;
import static com.devinsterling.localize.fx.test.TestUtil.getLocalizeFXInstance;

import static org.junit.jupiter.api.Assertions.*;

class LocalizeFXTest {

    @Test void testDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        assertEquals(defaultLocale, LocalizeFX.of().getLocale());
        assertEquals(defaultLocale, LocalizeFX.of(new LocalizeConfig()).getLocale());
    }

    @Test void testDefaultConfig() {
        LocalizeConfig defaultConfig = new LocalizeConfig();
        assertEquals(defaultConfig, LocalizeFX.of().getConfig());
        assertEquals(defaultConfig, LocalizeFX.of(Locale.ENGLISH).getConfig());
    }

    @Test void testBindingDuplicateLocale() {
        LocalizeFX localize = getLocalizeFXInstance();
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.setLocale(Locale.KOREAN);
        assertEquals("클릭!", binding.get());

        localize.setLocale(Locale.KOREAN);
        assertEquals("클릭!", binding.get());

        for (int i = 0; i < 100; i++) {
            localize.setLocale(Locale.JAPANESE);
        }
        assertEquals("クリック！", binding.get());

        localize.setLocale(Locale.ENGLISH);
        assertEquals("Click!", binding.get());
    }

    @Test void testLocaleProperty() {
        LocalizeFX localize = LocalizeFX.of();
        ObjectProperty<Locale> localeProperty = localize.localeProperty();

        assertEquals(Locale.getDefault(), localeProperty.get());

        localeProperty.set(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, localeProperty.get());

        assertThrows(NullPointerException.class, () -> localeProperty.set(null));

        assertEquals(Locale.JAPANESE, localeProperty.get());

        assertEquals(Locale.ENGLISH, LocalizeFX.of(Locale.ENGLISH).localeProperty().get());
    }

    @Test void testPutBundleProviderRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.KOREAN);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("클릭!", binding.get());
    }

    @Test void testPutBundleProviderReplaceRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("Click!", binding.get());

        localize.putBundleProvider("provider", TEST2_PROVIDER);
        assertEquals("Click!?", binding.get());
    }

    @Test void testRemoveBundleProviderRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeBundleProvider("provider");
        assertEquals("", binding.get());
    }

    @Test void testRemoveBundleProviderMissingKey() {
        LocalizeFX localize = LocalizeFX.of(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeBundleProvider("");
        localize.removeBundleProvider("Provider");
        assertEquals("クリック！", binding.get());
    }

    @Test void testRefreshNoProviders() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.refresh();
        assertEquals("", binding.get());

        localize.refresh("provider");
        assertEquals("", binding.get());
    }

    @Test void testRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);

        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("Click!", binding.get());

        localize.refresh();
        assertEquals("Click!", binding.get());

        localize.refresh("provider");
        assertEquals("Click!", binding.get());

        localize.refresh("nonexistent");
        assertEquals("Click!", binding.get());
    }

    @Test void testRefreshUpdatesBundle() {
        Function<String, ResourceBundle> bundleFactory = value -> new ListResourceBundle() {
            @Override protected Object[][] getContents() {
                return new Object[][] {{ TEST_KEY_CLICK_LABEL, value }};
            }
        };

        AtomicReference<ResourceBundle> currentBundle = new AtomicReference<>(bundleFactory.apply("abc"));
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_LABEL);

        localize.putBundleProvider("provider", locale -> currentBundle.get());
        assertEquals("abc", binding.get());

        // Realistically, this would be the file changing on disk or similar
        currentBundle.set(bundleFactory.apply("xyz"));
        assertEquals("abc", binding.get());

        localize.refresh("provider");
        assertEquals("xyz", binding.get());
    }
}
