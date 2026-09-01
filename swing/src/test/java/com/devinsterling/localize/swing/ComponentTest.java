package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolTip;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;
import javax.swing.text.JTextComponent;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class ComponentTest {

    @Test void testUnsupportedBindComponent() {
        LocalizeSwing localize = LocalizeSwing.of();
        SwingLocalizationValueBuilder<?> builder = localize.get(TEST_KEY_CLICK_LABEL);

        assertThrows(NullPointerException.class, () -> builder.bind(null));
        assertThrows(NullPointerException.class, () -> builder.bindTooltip(null));
        assertThrows(IllegalArgumentException.class, () -> builder.bind(new Component() {}));
    }

    @Test void testBindComponentTooltip() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JComponent component = new JComponent() {};

        localize.get(TEST_KEY_PRINT)
                .arg(1337)
                .bindTooltip(component);

        assertEquals("Output: 1,337", component.getToolTipText());

        localize.setLocale(Locale.KOREAN);

        assertEquals("출력: 1,337", component.getToolTipText());
    }

    @Test void testBindingTextDocumentChange() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JTextField text = new JTextField();

        localize.bind(TEST_KEY_CLICK_ME, text);

        // The binding must be retained even after changing the document
        text.setDocument(new PlainDocument());
        assertEquals("Click!", text.getText());
    }

    @Test void testArgumentTextDocumentChange() {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JTextField textArgument = new JTextField("abc");
        JTextField text = new JTextField();

        localize.get(TEST_KEY_PRINT)
                .arg(textArgument)
                .bind(text);

        // The argument binding must be retained even after changing the document
        textArgument.setDocument(new PlainDocument());
        assertEquals("Output: ", text.getText());

        textArgument.setText("xyz");
        assertEquals("Output: xyz", text.getText());
    }

    @MethodSource("bindableComponents")
    @ParameterizedTest void testBindComponent(Component component, Function<Object, String> textExtractor) {
        LocalizeSwing localize = getLocalizeSwingInstance();

        localize.bind(TEST_KEY_CLICK_ME, component);

        assertEquals("Click!", textExtractor.apply(component));
    }

    @MethodSource("argumentComponents")
    @ParameterizedTest void testArgumentComponent(Component countComponent, Runnable incrementCountComponent) {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();

        localize.get(TEST_KEY_PRINT)
                .arg(countComponent)
                .bind(label);

        assertEquals("Output: 0", label.getText());

        incrementCountComponent.run();
        assertEquals("Output: 1", label.getText());

        incrementCountComponent.run();
        incrementCountComponent.run();
        assertEquals("Output: 3", label.getText());
    }

    static Stream<Arguments> bindableComponents() {
        return Stream.of(
            bindableComponent(new Frame(), Frame::getTitle),
            bindableComponent(new Dialog(new Frame()), Dialog::getTitle),
            bindableComponent(new JLabel(), JLabel::getText),
            bindableComponent(new JButton(), AbstractButton::getText),
            bindableComponent(new JTextField(), JTextComponent::getText),
            bindableComponent(new JProgressBar(), JProgressBar::getString),
            bindableComponent(new JToolTip(), JToolTip::getTipText)
        );
    }

    static <T> Arguments bindableComponent(T component, Function<T, String> getter) {
        return Arguments.of(component, getter);
    }

    static Stream<Arguments> argumentComponents() {
        return Stream.of(
            bindableStringArgument(new Frame(), Frame::setTitle),
            bindableStringArgument(new Dialog(new Frame()), Dialog::setTitle),
            bindableStringArgument(new JLabel(), JLabel::setText),
            bindableStringArgument(new JButton(), AbstractButton::setText),
            bindableStringArgument(new JTextField(), JTextComponent::setText),
            bindableStringArgument(new JToolTip(), JToolTip::setTipText),
            bindableIntegerArgument(new JProgressBar(), JProgressBar::setValue),
            bindableIntegerArgument(new JSlider(0, 100, 0), JSlider::setValue),
            bindableIntegerArgument(new JSpinner(), JSpinner::setValue),
            bindableComboBoxArgument(new JComboBox<>())
        );
    }

    static <T> Arguments bindableStringArgument(T component, BiConsumer<T, String> setter) {
        AtomicInteger count = new AtomicInteger();
        setter.accept(component, "0");

        return Arguments.of(
            component,
            (Runnable) () -> setter.accept(component, String.valueOf(count.incrementAndGet()))
        );
    }

    static <T> Arguments bindableIntegerArgument(T component, BiConsumer<T, Integer> setter) {
        AtomicInteger count = new AtomicInteger();
        setter.accept(component, 0);

        return Arguments.of(component, (Runnable) () -> setter.accept(component, count.incrementAndGet()));
    }

    static Arguments bindableComboBoxArgument(JComboBox<Integer> component) {
        AtomicInteger count = new AtomicInteger();

        for (int i = 0; i < 100; i++) {
            component.addItem(i);
        }

        return Arguments.of(component, (Runnable) () -> component.setSelectedItem(count.incrementAndGet()));
    }
}
