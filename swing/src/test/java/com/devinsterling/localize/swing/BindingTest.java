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

    @Test void failedBindingIsNotRegistered() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        FaultyText faulty = new FaultyText(true);

        assertThrows(
            FaultyText.FaultyException.class,
            () -> localize.get(TEST_KEY_CLICK_ME).bind(faulty, FaultyText::setText)
        );

        // Since the previous call failed, the binding was not registered,
        // so no exception will be thrown here
        assertDoesNotThrow(() -> localize.setLocale(Locale.JAPANESE));
    }

    @Test void aggregateExceptionCannotSuppressItself() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        FaultyText.FaultyException exception = new FaultyText.FaultyException();
        FaultyText faulty1 = new FaultyText(false, exception);
        FaultyText faulty2 = new FaultyText(false, exception);

        localize.get(TEST_KEY_CLICK_ME).bind(faulty1, FaultyText::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(faulty2, FaultyText::setText);

        faulty1.throwOnSetText = faulty2.throwOnSetText = true;

        var caught = assertThrows(
            FaultyText.FaultyException.class,
            () -> localize.setLocale(Locale.JAPANESE)
        );

        assertSame(caught, exception);
        assertEquals(0, caught.getSuppressed().length);
    }

    @Test void aggregateBindingExceptions() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        FaultyText faulty1 = new FaultyText(false);
        FaultyText faulty2 = new FaultyText(false);
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();

        localize.get(TEST_KEY_CLICK_ME).bind(faulty1, FaultyText::setText);
        localize.bind(TEST_KEY_CLICK_ME, label1);
        localize.get(TEST_KEY_CLICK_ME).bind(faulty2, FaultyText::setText);
        localize.bind(TEST_KEY_CLICK_ME, label2);

        faulty1.throwOnSetText = faulty2.throwOnSetText = true;

        var exception = assertThrows(
            FaultyText.FaultyException.class,
            () -> localize.setLocale(Locale.JAPANESE)
        );

        assertSame(faulty1.exception, exception);
        assertArrayEquals(new Throwable[] { faulty2.exception }, exception.getSuppressed());

        // Despite the exception, valid bindings are still resolved
        assertEquals("クリック！", label1.getText());
        assertEquals(label1.getText(), label2.getText());
    }

    private static class FaultyText {
        private final FaultyException exception;
        private boolean throwOnSetText;

        public FaultyText(boolean throwOnSetText) {
            this(throwOnSetText, new FaultyException());
        }

        public FaultyText(boolean throwOnSetText, FaultyException exception) {
            this.throwOnSetText = throwOnSetText;
            this.exception = exception;
        }

        private void setText(String text) {
            if (throwOnSetText) {
                throw exception;
            }
        }

        private static class FaultyException extends RuntimeException {}
    }
}
