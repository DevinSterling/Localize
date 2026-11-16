package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.fixtures.TextSetter;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;

import java.util.Locale;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
class BindingTest {

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

    @SuppressWarnings({ "UnusedAssignment" })
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
        TextSetter faulty = new TextSetter().throwOnSetText(true);

        assertThrows(
            TextSetter.SetTextException.class,
            () -> localize.get(TEST_KEY_CLICK_ME).bind(faulty, TextSetter::setText)
        );

        // Since the previous call failed, the binding was not registered,
        // so no exception will be thrown here
        assertDoesNotThrow(() -> localize.setLocale(Locale.JAPANESE));
    }

    @Test void aggregateExceptionCannotSuppressItself() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        TextSetter.SetTextException exception = new TextSetter.SetTextException();
        TextSetter faulty1 = new TextSetter().exception(exception);
        TextSetter faulty2 = new TextSetter().exception(exception);

        localize.get(TEST_KEY_CLICK_ME).bind(faulty1, TextSetter::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(faulty2, TextSetter::setText);

        faulty1.throwOnSetText = faulty2.throwOnSetText = true;

        var caught = assertThrows(
            TextSetter.SetTextException.class,
            () -> localize.setLocale(Locale.JAPANESE)
        );

        assertSame(caught, exception);
        assertEquals(0, caught.getSuppressed().length);
    }

    @Test void aggregateBindingExceptions() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        TextSetter faulty1 = new TextSetter();
        TextSetter faulty2 = new TextSetter();
        TextSetter text1 = new TextSetter();
        TextSetter text2 = new TextSetter();

        localize.get(TEST_KEY_CLICK_ME).bind(faulty1, TextSetter::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(text1, TextSetter::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(faulty2, TextSetter::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(text2, TextSetter::setText);

        faulty1.throwOnSetText = faulty2.throwOnSetText = true;

        var exception = assertThrows(
            TextSetter.SetTextException.class,
            () -> localize.setLocale(Locale.JAPANESE)
        );

        assertSame(faulty1.exception, exception);
        assertArrayEquals(new Throwable[] { faulty2.exception }, exception.getSuppressed());

        // Despite the exception, valid bindings are still resolved
        assertEquals("クリック！", text1.text);
        assertEquals(text1.text, text2.text);
    }
}
