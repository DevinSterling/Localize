package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JButton;
import javax.swing.JLabel;

import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class SwingLocalizationValueBuilderTest {

    @Test void testWeakArguments() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        AtomicInteger counter = new AtomicInteger(100);
        JLabel label = new JLabel();

        StringBinding binding = localize.get(TEST_KEY_PRINT)
                .arg(counter, AtomicInteger::get)
                .bind(label);

        assertEquals("Output: 100", label.getText());

        counter.set(200);
        counter = null;
        awaitGarbageCollection();

        // The counter is garbage collected, but the binding is unaware
        // until it is updated (e.g., manual, locale change, etc.)
        assertEquals("Output: 100", label.getText());

        binding.update();
        assertEquals("Output: null", label.getText());
    }

    @Test void testStrongArguments() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        AtomicInteger counter = new AtomicInteger(100);
        JLabel label = new JLabel();

        StringBinding binding = localize.get(TEST_KEY_PRINT)
                .arg(counter::get)
                .bind(label);

        assertEquals("Output: 100", label.getText());

        counter.set(200);
        counter = null;
        awaitGarbageCollection();

        assertEquals("Output: 100", label.getText());

        binding.update();
        assertEquals("Output: 200", label.getText());
    }

    @Test void testTriggerArguments() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        AtomicInteger count = new AtomicInteger();
        JLabel label = new JLabel();
        JButton button = new JButton();

        localize.get(TEST_KEY_PRINT)
                .arg(count::get)
                .on(Trigger.action(button))
                .bind(label);

        button.addActionListener(e -> count.incrementAndGet());

        assertEquals("Output: 0", label.getText());

        button.doClick();
        count.incrementAndGet();
        button.doClick();
        count.incrementAndGet();

        assertEquals("Output: 3", label.getText());
    }
}
