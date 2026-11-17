package com.devinsterling.localize.swing;

import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.PlainDocument;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
class SwingListenerBindingHelperTest {

    @Test void addDocumentListenerReleasesBindingAndSelfRemoves() {
        JTextField textField = new JTextField();
        IntSupplier listenerCount = () -> textField.getPropertyChangeListeners("document").length;
        int baseline = listenerCount.getAsInt();

        WeakReference<TestBinding> weakBinding = TestBinding.install(
            binding ->  SwingListenerBindingHelper.addDocumentListener(textField, binding)
        );
        assertEquals(baseline + 1, listenerCount.getAsInt());

        awaitGarbageCollection();
        assertNull(weakBinding.get());

        textField.setDocument(new PlainDocument());
        assertEquals(baseline, listenerCount.getAsInt());
    }

    @MethodSource("listenerCases")
    @ParameterizedTest void installListenersReleasesBindingAndSelfRemoves(Case testCase) {
        // The initial count isn't always 0 (e.g., JComboBox initializes with 1)
        int baseline = testCase.listenersCount.getAsInt();

        SwingListenerBindingHelper helper = new SwingListenerBindingHelper();
        testCase.setup.accept(helper);

        WeakReference<TestBinding> weakBinding = TestBinding.install(helper::installListeners);
        assertEquals(baseline + 1, testCase.listenersCount.getAsInt());

        awaitGarbageCollection();
        assertNull(weakBinding.get());

        // Because the installed binding is short-lived and now out of scope,
        // the associated installed listener is removed on the next change fired
        testCase.fireChange.run();
        assertEquals(baseline, testCase.listenersCount.getAsInt());
    }

    static Stream<Case> listenerCases() {
        JLabel label = new JLabel();
        JTextField textField = new JTextField();
        JTextField docSwapField = new JTextField();
        JSlider slider = new JSlider();
        JComboBox<String> combo = new JComboBox<>(new String[] { "a", "b" });
        JButton button = new JButton();

        return Stream.of(
            new Case(
                "PropertyListener",
                h -> h.addPropertyListener("text", label),
                () -> label.setText("b"),
                () -> label.getPropertyChangeListeners("text").length
            ),
            new Case(
                "TextDocumentListener",
                h -> h.addTextListener(textField),
                () -> textField.setText("b"),
                () -> ((AbstractDocument) textField.getDocument()).getDocumentListeners().length
            ),
            new Case(
                "TextDocumentSwapListener",
                h -> h.addTextListener(docSwapField),
                () -> docSwapField.setDocument(new PlainDocument()),
                () -> docSwapField.getPropertyChangeListeners("document").length
            ),
            new Case(
                "ChangeListener",
                h -> h.addChangeListener(slider, JSlider::addChangeListener, JSlider::removeChangeListener),
                () -> slider.setValue(1),
                () -> slider.getChangeListeners().length
            ),
            new Case(
                "ItemListener",
                h -> h.addItemListener(combo, JComboBox::addItemListener, JComboBox::removeItemListener),
                () -> combo.setSelectedIndex(1),
                () -> combo.getItemListeners().length
            ),
            new Case(
                "TriggerListener",
                h -> h.addTrigger(Trigger.action(button)),
                button::doClick,
                () -> button.getActionListeners().length
            )
        );
    }

    private record Case(
        // Convenience for showing the test case name in the terminal when viewing the results from JUnit
        String name,
        Consumer<SwingListenerBindingHelper> setup,
        Runnable fireChange,
        IntSupplier listenersCount
    ) {
        @Override public String toString() {
            return name;
        }
    }

    private static final class TestBinding implements Binding<String> {
        private static WeakReference<TestBinding> install(Consumer<TestBinding> consumer) {
            TestBinding binding = new TestBinding();
            consumer.accept(binding);
            return new WeakReference<>(binding);
        }

        @Override public void update() {
            // no-op
        }

        @Override public void dispose() {
            // no-op
        }

        @Override public String get() {
            return "";
        }

        @Override public boolean isActive() {
            return true;
        }
    }
}
