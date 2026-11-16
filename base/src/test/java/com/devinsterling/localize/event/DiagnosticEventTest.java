package com.devinsterling.localize.event;

import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.MissingValueHandler;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticEventTest {

    @Test void testOnMissingValue() {
        LocalizeEventCapturer<DiagnosticEvent.MissingValue> localize
                = new LocalizeEventCapturer<>(DiagnosticEvent.MissingValue.class);
        localize.setFormatter(request -> null);

        LocalizationRequestSource.Key key = new LocalizationRequestSource.Key("abc");
        LocalizationRequest request = LocalizationRequest.Builder.of(key).build();
        MissingValueHandler handler = MissingValueHandler.of("123");

        localize.format("Hi {user}").value();
        localize.getValue(TEST_KEY_GREET); // Event 0
        localize.get("non-existent").defaultHandler(handler).value(); // 1
        localize.formatValue(request); // 2

        assertEquals(4, localize.capturedEventsCount());

        DiagnosticEvent.MissingValue event0 = localize.getEvent(0);
        assertEquals(new LocalizationRequestSource.Pattern("Hi {user}"), event0.getRequest().getSource());

        DiagnosticEvent.MissingValue event1 = localize.getEvent(1);
        assertEquals(new LocalizationRequestSource.Key(TEST_KEY_GREET), event1.getRequest().getSource());

        DiagnosticEvent.MissingValue event2 = localize.getEvent(2);
        assertEquals(new LocalizationRequestSource.Key("non-existent"), event2.getRequest().getSource());
        assertSame(handler, event2.getRequest().getMissingValueHandler());

        DiagnosticEvent.MissingValue event3 = localize.getEvent(3);
        assertSame(key, event3.getRequest().getSource());
        assertSame(request, event3.getRequest());
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
