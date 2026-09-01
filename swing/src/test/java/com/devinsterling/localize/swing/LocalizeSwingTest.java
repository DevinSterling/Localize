package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class LocalizeSwingTest {

    @Test void testNullPropertyChangeListener() {
        LocalizeSwing localize = LocalizeSwing.of();

        // Passing `null` is a no-op to maintain parity with Swing regarding listeners; No NPE
        localize.addPropertyChangeListener(null);
        localize.addLocaleListener(null);
        localize.removePropertyChangeListener(null);
        localize.removeLocaleListener(null);
    }

    @Test void testPropertyChangeListenerClassicLifecycle() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
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
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);

        localize.addLocaleListener(((oldLocale, newLocale) -> {
            assertNotEquals(oldLocale, newLocale);
            assertEquals(newLocale, localize.getLocale());
        }));

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.KOREAN);
        localize.setLocale(Locale.ENGLISH);
    }

    @Test void testAddBundleProviderRefresh() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.KOREAN);
        JLabel label = new JLabel();

        localize.bind(TEST_KEY_CLICK_ME, label);
        assertEquals("", label.getText());

        localize.addBundleProvider(TEST_PROVIDER);
        assertEquals("클릭!", label.getText());
    }

    @Test void testReplaceBundleProviderRefresh() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("Click!", label.getText());

        localize.putBundleProvider("provider", TEST2_PROVIDER);
        assertEquals("Click!?", label.getText());
    }

    @Test void testRemoveBundleProviderRefresh() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.removeBundleProvider("provider");
        assertEquals("", label.getText());
    }

    @Test void testRemoveBundleProviderMissingKey() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.removeBundleProvider("");
        localize.removeBundleProvider("Provider");
        assertEquals("クリック！", label.getText());
    }

    @Test void testRefreshNoProviders() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.refresh();
        assertEquals("", label.getText());

        localize.refresh("provider");
        assertEquals("", label.getText());
    }

    @Test void testRefresh() {
        LocalizeSwing localize = LocalizeSwing.of(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        assertEquals("", label.getText());

        localize.putBundleProvider("provider", TEST_PROVIDER);
        assertEquals("クリック！", label.getText());

        localize.refresh();
        assertEquals("クリック！", label.getText());

        localize.refresh("provider");
        assertEquals("クリック！", label.getText());

        localize.refresh("nonexistent");
        assertEquals("クリック！", label.getText());
    }

    @Test void testRefreshUpdatesBundle() {
        Function<String, ResourceBundle> bundleFactory = value -> new ListResourceBundle() {
            @Override protected Object[][] getContents() {
                return new Object[][] {{ TEST_KEY_CLICK_ME, value }};
            }
        };

        AtomicReference<ResourceBundle> currentBundle = new AtomicReference<>(bundleFactory.apply("abc"));
        LocalizeSwing localize = LocalizeSwing.of(Locale.JAPANESE);
        JLabel label = new JLabel();
        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.putBundleProvider("provider", locale -> currentBundle.get());
        assertEquals("abc", label.getText());

        // Realistically, this would be the file changing on disk or similar
        currentBundle.set(bundleFactory.apply("xyz"));
        assertEquals("abc", label.getText());

        localize.refresh("provider");
        assertEquals("xyz", label.getText());
    }
}
