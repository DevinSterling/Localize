package com.devinsterling.localize.swing;

import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.ProviderEvent;
import com.devinsterling.localize.swing.fixtures.PropertyChangeListenerProbe;
import com.devinsterling.localize.swing.fixtures.TextSetter;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Locale;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class LocalizeSwingHookTest {

    @Test void noProviderDiagnosticEventReceived() {
        class LocalizeTest extends LocalizeSwing {
            final ArrayList<ProviderEvent> captured = new ArrayList<>();
            int computations;

            LocalizeTest() {
                super(Locale.ENGLISH, new LocalizeConfig());
            }

            @Override protected void notifyListeners() {
                computations++;
                super.notifyListeners();
            }

            @Override protected void onProviderEvent(ProviderEvent event) {
                captured.add(event);
                super.onProviderEvent(event);
            }

            void fireProviderEvent(ProviderEvent event) {
                fireEvent(event);
            }
        }
        interface ProviderDiagnosticEvent extends ProviderEvent, DiagnosticEvent {}

        LocalizeTest localize = new LocalizeTest();
        ProviderDiagnosticEvent event = () -> localize;

        localize.fireProviderEvent(event);

        // Despite receiving the diagnostic event, it mustn't notify listeners
        assertEquals(0, localize.computations);
        assertEquals(1, localize.captured.size());
        assertSame(event, localize.captured.get(0));
    }

    @Test void onLocaleReplacedAggregateExceptions() {
        LocalizeSwing localize = getLocalizeSwingInstance();

        var faultyText = new TextSetter();
        var text = new TextSetter();
        var faultyListener = new PropertyChangeListenerProbe().throwOnCall(true);
        var listener = new PropertyChangeListenerProbe();

        localize.get(TEST_KEY_CLICK_ME).bind(faultyText, TextSetter::setText);
        localize.get(TEST_KEY_CLICK_ME).bind(text, TextSetter::setText);
        localize.addPropertyChangeListener(faultyListener);
        localize.addPropertyChangeListener(listener);

        faultyText.throwOnSetText(true);
        var caught = assertThrows(RuntimeException.class, () -> localize.setLocale(Locale.JAPANESE));

        assertSame(faultyText.exception, caught);
        assertArrayEquals(new Throwable[] { faultyListener.exception }, caught.getSuppressed());
        assertEquals(0, faultyListener.count);
        assertEquals(1, listener.count);
        // `faultyText` and `text` +1 as `Localize.bind` calls `setText` once
        assertEquals(1, faultyText.count);
        assertEquals(2, text.count);
    }

    @Test void onLocaleReplacedAggregateExceptionsCannotSuppressItself() {
        LocalizeSwing localize = getLocalizeSwingInstance();

        RuntimeException shared = new RuntimeException();
        var faultyText = new TextSetter().exception(shared);
        var faultyListener = new PropertyChangeListenerProbe().throwOnCall(true).exception(shared);

        localize.get(TEST_KEY_CLICK_ME).bind(faultyText, TextSetter::setText);
        localize.addPropertyChangeListener(faultyListener);

        faultyText.throwOnSetText(true);
        var caught = assertThrows(RuntimeException.class, () -> localize.setLocale(Locale.JAPANESE));

        assertSame(shared, caught);
        assertArrayEquals(new Throwable[] {}, caught.getSuppressed());
        assertEquals(0, faultyListener.count);
        assertEquals(1, faultyText.count);
    }

    @Test void onLocaleReplacedAggregatePropertyChangeListenerExceptions() {
        LocalizeSwing localize = getLocalizeSwingInstance();

        var faultyListener1 = new PropertyChangeListenerProbe().throwOnCall(true);
        var faultyListener2 = new PropertyChangeListenerProbe().throwOnCall(true);
        var listener = new PropertyChangeListenerProbe();

        localize.addPropertyChangeListener(faultyListener1);
        localize.addPropertyChangeListener(faultyListener2);
        localize.addPropertyChangeListener(listener);

        var caught = assertThrows(RuntimeException.class, () -> localize.setLocale(Locale.JAPANESE));

        assertSame(faultyListener1.exception, caught);
        assertArrayEquals(new Throwable[] { faultyListener2.exception }, caught.getSuppressed());
        assertEquals(0, faultyListener1.count);
        assertEquals(0, faultyListener2.count);
        assertEquals(1, listener.count);
    }
}
