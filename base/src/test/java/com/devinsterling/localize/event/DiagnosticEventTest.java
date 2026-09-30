package com.devinsterling.localize.event;

import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;

import com.devinsterling.localize.MissingValueHandler;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class DiagnosticEventTest {

    @Test void testOnMissingKey() {
        LocalizeEventCapturer<DiagnosticEvent.MissingKey> localize
                = new LocalizeEventCapturer<>(DiagnosticEvent.MissingKey.class);

        LocalizationRequestSource.Key key = new LocalizationRequestSource.Key("");
        LocalizationRequest request = LocalizationRequest.Builder.of(key).build();
        MissingValueHandler handler = MissingValueHandler.of("123");

        localize.getValue(TEST_KEY_GREET); // Event 0
        localize.addProvider(TEST_PROVIDER);
        localize.get(TEST_KEY_GREET);
        localize.get("non-existent").defaultHandler(handler).value(); // 1
        localize.formatValue(request); // 2

        assertEquals(3, localize.capturedEventsCount());

        DiagnosticEvent.MissingKey event0 = localize.getEvent(0);
        assertEquals(TEST_KEY_GREET, event0.getKey().value());

        DiagnosticEvent.MissingKey event1 = localize.getEvent(1);
        assertEquals("non-existent", event1.getKey().value());
        assertEquals(handler, event1.getRequest().getMissingValueHandler());

        DiagnosticEvent.MissingKey event2 = localize.getEvent(2);
        assertSame(key, event2.getKey());
        assertSame(request, event2.getRequest());
    }

    @Test void testOnListenerException() {
        class ListenerTestException extends RuntimeException {}

        LocalizeEventCapturer<DiagnosticEvent.ListenerExceptionCaught> localize
                = new LocalizeEventCapturer<>(DiagnosticEvent.ListenerExceptionCaught.class);

        ListenerTestException listenerException = new ListenerTestException();
        EventListener<LocalizeEvent> listener = request -> {
            throw listenerException;
        };

        localize.addListener(LocalizeEvent.class, listener);
        assertThrows(ListenerTestException.class, () -> localize.setLocale(Locale.JAPANESE));
        assertThrows(ListenerTestException.class, () -> localize.setFormatter(request -> null));

        assertEquals(2, localize.capturedEventsCount());

        DiagnosticEvent.ListenerExceptionCaught event0 = localize.getEvent(0);
        assertSame(listener, event0.getListener());
        assertSame(listenerException, event0.getException());
        assertInstanceOf(LocaleEvent.Replaced.class, event0.getEvent());
        
        DiagnosticEvent.ListenerExceptionCaught event1 = localize.getEvent(1);
        assertSame(listener, event1.getListener());
        assertSame(listenerException, event1.getException());
        assertInstanceOf(FormatterEvent.Replaced.class, event1.getEvent());
    }
}
