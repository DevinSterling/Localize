package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.ProviderEvent;
import com.devinsterling.localize.fx.junit.JavaFXExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
public class LocalizeFXHookTest {

    @Test void noProviderDiagnosticEventReceived() {
        class LocalizeTest extends LocalizeFX {
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
}
