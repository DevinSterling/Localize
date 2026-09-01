package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;

import java.util.Locale;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class BindingTest {

    @Test void testActiveBinding() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();

        StringBinding binding = localize.bind(TEST_KEY_CLICK_ME, label);
        assertTrue(binding.isActive());
    }

    @Test void testBindingAutomaticUpdate() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();
        StringBinding binding = localize.bind(TEST_KEY_CLICK_ME, label);

        assertEquals("Click!", label.getText());
        assertEquals(label.getText(), binding.get());

        localize.setLocale(Locale.JAPANESE);

        assertEquals("クリック！", label.getText());
        assertEquals(label.getText(), binding.get());
    }

    @Test void testNoUpdateAfterDisposeBinding() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();
        StringBinding binding = localize.bind(TEST_KEY_CLICK_ME, label);
        String original = label.getText();

        binding.dispose();
        localize.setLocale(Locale.JAPANESE);
        // Bindings update automatically whenever the locale changes. However, manually invoke an update
        binding.update();

        assertEquals(original, label.getText());
        assertEquals(original, binding.get());
    }

    @Test void testDisposeBinding() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();
        StringBinding binding = localize.bind(TEST_KEY_CLICK_ME, label);

        binding.dispose();
        // Calling dispose is idempotent
        binding.dispose();

        assertFalse(binding.isActive());
    }

    @Test void testGarbageCollectedBinding() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();

        StringBinding binding = localize.bind(TEST_KEY_CLICK_ME, label);
        label = null;

        awaitGarbageCollection();

        // `label` has been garbage collected, so the binding must not be active
        assertFalse(binding.isActive());
    }
}
