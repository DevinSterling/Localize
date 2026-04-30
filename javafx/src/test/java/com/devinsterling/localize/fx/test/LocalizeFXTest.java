package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.fx.LocalizeFX;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.fx.test.TestUtil.*;
import static com.devinsterling.localize.fx.test.TestUtil.getLocalizeFXInstance;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeFXTest {

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

    @Test public void testPutBundleProviderRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.KOREAN);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("클릭!", binding.get());
    }

    @Test public void testPutBundleProviderReplaceRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("Click!", binding.get());

        localize.putBundleProvider("provider", TEST2_PROVIDER);
        assertEquals("Click!?", binding.get());
    }

    @Test public void testRemoveBundleProviderRefresh() {
        LocalizeFX localize = LocalizeFX.of(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeBundleProvider("provider");
        assertEquals("", binding.get());
    }

    @Test public void testRemoveBundleProviderMissingKey() {
        LocalizeFX localize = LocalizeFX.of(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeBundleProvider("");
        localize.removeBundleProvider("Provider");
        assertEquals("クリック！", binding.get());
    }

    @Test public void testRefreshNoProviders() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.refresh();
        assertEquals("", binding.get());

        localize.refresh("provider");
        assertEquals("", binding.get());
    }

    @Test public void testRefresh() {
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
}
