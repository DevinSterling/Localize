package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.PatternSource;
import com.devinsterling.localize.fx.junit.JavaFXExtension;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static com.devinsterling.localize.fx.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class LocalizeFXTest {
    Localize.ProviderKey providerKey = Localize.ProviderKey.of("provider");

    @Test void testCompositionConstructor() {
        Localize localize = Localize.of();
        LocalizeFX swingA = localize.attach(LocalizeFX::new);
        LocalizeFX swingB = localize.attach(LocalizeFX::new);

        assertNotEquals(localize, swingA);
        assertNotEquals(swingA, swingB);
    }

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

    @Test void testPutProviderRefresh() {
        LocalizeFX localize = getEmptyLocalizeFXInstance(Locale.KOREAN);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.addProvider(TEST_PROVIDER);
        assertEquals("클릭!", binding.get());
    }

    @Test void testReplaceProviderRefresh() {
        LocalizeFX localize = getEmptyLocalizeFXInstance();
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("Click!", binding.get());

        localize.putProvider(providerKey, TEST2_PROVIDER);
        assertEquals("Click!?", binding.get());
    }

    @Test void testRemoveProviderRefresh() {
        LocalizeFX localize = getEmptyLocalizeFXInstance(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeProvider(providerKey);
        assertEquals("", binding.get());
    }

    @Test void testRemoveProviderMissingKey() {
        LocalizeFX localize = getEmptyLocalizeFXInstance(Locale.JAPANESE);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("クリック！", binding.get());

        localize.removeProvider(Localize.ProviderKey.of(""));
        localize.removeProvider(Localize.ProviderKey.of("Provider"));
        assertEquals("クリック！", binding.get());
    }

    @Test void testRefreshNoProviders() {
        LocalizeFX localize = getEmptyLocalizeFXInstance();
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.refreshProviders();
        assertEquals("", binding.get());

        localize.refreshProvider(providerKey);
        assertEquals("", binding.get());
    }

    @Test void testRefreshProviders() {
        LocalizeFX localize = getEmptyLocalizeFXInstance();

        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);
        assertEquals("", binding.get());

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("Click!", binding.get());

        localize.refreshProviders();
        assertEquals("Click!", binding.get());

        localize.refreshProvider(providerKey);
        assertEquals("Click!", binding.get());

        localize.refreshProvider(Localize.ProviderKey.of("non-existent"));
        assertEquals("Click!", binding.get());
    }

    @Test void testRefreshUpdatesBundle() {
        AtomicReference<PatternSource> currentSource = new AtomicReference<>(key -> "abc");
        LocalizeFX localize = getEmptyLocalizeFXInstance();
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_LABEL);

        localize.putProvider(providerKey, locale -> currentSource.get());
        assertEquals("abc", binding.get());

        // Realistically, this would be the file changing on disk or similar
        currentSource.set(key -> "xyz");
        assertEquals("abc", binding.get());

        localize.refreshProvider(providerKey);
        assertEquals("xyz", binding.get());
    }

    @Test void testFormatterReplaced() {
        LocalizeFX localize = getEmptyLocalizeFXInstance();

        StringBinding binding = localize.format("Hello world!").binding();
        assertEquals("Hello world!", binding.get());

        localize.setFormatter(request -> "> " + LocalizationFormatter.STANDARD.format(request) + "!!!!");
        assertEquals("> Hello world!!!!!", binding.get());
    }
}
