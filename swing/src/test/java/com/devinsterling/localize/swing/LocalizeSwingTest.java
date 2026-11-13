package com.devinsterling.localize.swing;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.PatternSource;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class LocalizeSwingTest {
    private final Localize.ProviderKey providerKey = Localize.ProviderKey.of("provider");

    @Test void testCompositionConstructor() {
        Localize localize = Localize.of();
        LocalizeSwing swingA = localize.attach(LocalizeSwing::new);
        LocalizeSwing swingB = localize.attach(LocalizeSwing::new);

        assertNotEquals(localize, swingA);
        assertNotEquals(swingA, swingB);
    }

    @Test void testNullPropertyChangeListener() {
        LocalizeSwing localize = LocalizeSwing.of();

        // Passing `null` is a no-op to maintain parity with Swing regarding listeners; No NPE
        localize.addPropertyChangeListener(null);
        localize.addLocaleListener(null);
        localize.removePropertyChangeListener(null);
        localize.removeLocaleListener(null);
    }

    @Test void testPropertyChangeListenerClassicLifecycle() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();
        AtomicInteger counter = new AtomicInteger();

        LocaleChangeListener listener = (oldLocale, newLocale) -> counter.getAndIncrement();
        // no-op since it's not added yet.
        localize.removeLocaleListener(listener);

        // Duplicate listeners are treated as one
        localize.addLocaleListener(listener);
        localize.addLocaleListener(listener);
        localize.addLocaleListener(listener);
        localize.setLocale(Locale.CHINESE);

        localize.removeLocaleListener(listener);

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.ENGLISH);

        assertEquals(1, counter.get());
    }

    // NOTE:
    // Both the `String` and `LocalizationKey` overloads internally delegate to
    // the same exact method that returns a `SwingLocalizationValueBuilder`.
    @Test void testBindOverloadMethods() {
        String expected = "Click!";
        LocalizeSwing localize = getLocalizeSwingInstance();
        JTextField text = new JTextField();

        StringBinding binding1 = localize.bind(TEST_KEY_CLICK_ME, text);
        StringBinding binding2 = localize.bind(() -> TEST_KEY_CLICK_ME, text);
        assertEquals(expected, binding1.get());
        assertEquals(expected, binding2.get());

        StringBinding binding3 = localize.bindTooltip(TEST_KEY_CLICK_ME, text);
        StringBinding binding4 = localize.bindTooltip(() -> TEST_KEY_CLICK_ME, text);
        assertEquals(expected, binding3.get());
        assertEquals(expected, binding4.get());
    }

    @Test void testLocaleChange() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        assertEquals("Click!", label.getText());

        localize.setLocale(Locale.JAPANESE);
        assertEquals("クリック！", label.getText());

        localize.setLocale(Locale.KOREAN);
        assertEquals("클릭!", label.getText());
    }

    @Test void testLocaleChangeListener() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();

        localize.addLocaleListener(((oldLocale, newLocale) -> {
            assertNotEquals(oldLocale, newLocale);
            assertEquals(newLocale, localize.getLocale());
        }));

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.KOREAN);
        localize.setLocale(Locale.ENGLISH);
    }

    @Test void testAddProviderRefresh() {
        LocalizeSwing localize = TestUtil.getEmptyLocalizeSwingInstance(Locale.KOREAN);
        JLabel label = new JLabel();

        localize.bind(TEST_KEY_CLICK_ME, label);
        assertEquals("", label.getText());

        localize.addProvider(TEST_PROVIDER);
        assertEquals("클릭!", label.getText());
    }

    @Test void testReplaceBundleProviderRefresh() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("Click!", label.getText());

        localize.putProvider(providerKey, TEST2_PROVIDER);
        assertEquals("Click!?", label.getText());
    }

    @Test void testRemoveProviderRefresh() {
        LocalizeSwing localize = TestUtil.getEmptyLocalizeSwingInstance(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.removeProvider(providerKey);
        assertEquals("", label.getText());
    }

    @Test void testRemoveProviderMissingKey() {
        LocalizeSwing localize = TestUtil.getEmptyLocalizeSwingInstance(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.removeProvider(Localize.ProviderKey.of(""));
        localize.removeProvider(Localize.ProviderKey.of("Provider"));
        assertEquals("クリック！", label.getText());
    }

    @Test void testRefreshNoProviders() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.refreshProviders();
        assertEquals("", label.getText());

        localize.refreshProvider(providerKey);
        assertEquals("", label.getText());
    }

    @Test void testRefreshProvider() {
        LocalizeSwing localize = TestUtil.getEmptyLocalizeSwingInstance(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        assertEquals("", label.getText());

        localize.putProvider(providerKey, TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.refreshProviders();
        assertEquals("クリック！", label.getText());

        localize.refreshProvider(providerKey);
        assertEquals("クリック！", label.getText());

        localize.refreshProvider(Localize.ProviderKey.of("nonexistent"));
        assertEquals("クリック！", label.getText());
    }

    @Test void testRefreshUpdatesBundle() {
        AtomicReference<PatternSource> currentSource = new AtomicReference<>(key -> "abc");
        LocalizeSwing localize = TestUtil.getEmptyLocalizeSwingInstance(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putProvider(providerKey, locale -> currentSource.get());
        assertEquals("abc", label.getText());

        // Realistically, this would be the file changing on disk or similar
        currentSource.set(key -> "xyz");
        assertEquals("abc", label.getText());

        localize.refreshProvider(providerKey);
        assertEquals("xyz", label.getText());
    }

    @Test void testFormatterReplaced() {
        LocalizeSwing localize = getEmptyLocalizeSwingInstance();
        JLabel label = new JLabel();

        localize.format("Hello world!").bind(label);
        assertEquals("Hello world!", label.getText());

        localize.setFormatter(request -> "> " + LocalizationFormatter.STANDARD.format(request) + "!!!!");
        assertEquals("> Hello world!!!!!", label.getText());
    }
}
