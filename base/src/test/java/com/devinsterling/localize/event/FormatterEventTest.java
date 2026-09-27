package com.devinsterling.localize.event;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class FormatterEventTest {

    @Test void testOnFormatterReplaced() {
        LocalizeEventCapturer<FormatterEvent.Replaced> localize
                = new LocalizeEventCapturer<>(FormatterEvent.Replaced.class);
        Function<String, LocalizationFormatter> formatterFactory = s -> request -> s;

        LocalizationFormatter original = localize.getFormatter();
        LocalizationFormatter formatter1 = formatterFactory.apply("A");
        localize.setFormatter(formatter1);
        // A duplicate call will not fire an event
        localize.setFormatter(formatter1);

        LocalizationFormatter formatter2 = formatterFactory.apply("B");
        localize.setFormatter(formatter2);

        assertEquals(2, localize.capturedEventsCount());

        FormatterEvent.Replaced event0 = localize.getEvent(0);
        assertSame(original, event0.getOldFormatter());
        assertSame(formatter1, event0.getNewFormatter());

        FormatterEvent.Replaced event1 = localize.getEvent(1);
        assertSame(formatter1, event1.getOldFormatter());
        assertSame(formatter2, event1.getNewFormatter());
    }

    @Test void testOnFormatterException() {
        class FormatterTestException extends RuntimeException {}

        LocalizeEventCapturer<FormatterEvent.ExceptionCaught> localize
                = new LocalizeEventCapturer<>(FormatterEvent.ExceptionCaught.class);

        FormatterTestException formatterException = new FormatterTestException();
        LocalizationFormatter formatter = request -> {
            throw formatterException;
        };
        
        LocalizationRequestSource.Pattern pattern = new LocalizationRequestSource.Pattern("");
        LocalizationRequest request = LocalizationRequest.Builder.of(pattern).build();

        localize.addProvider(TEST_PROVIDER);
        localize.setFormatter(formatter);
        assertThrows(FormatterTestException.class, () -> localize.getValue(TEST_KEY_OUTPUT)); // Event 0
        assertThrows(FormatterTestException.class, () -> localize.formatValue(request)); // 1

        assertEquals(2, localize.capturedEventsCount());

        FormatterEvent.ExceptionCaught event0 = localize.getEvent(0);
        assertEquals("Output: {0}", event0.getRequest().getPattern());
        assertSame(formatter, event0.getFormatter());
        assertSame(formatterException, event0.getException());

        FormatterEvent.ExceptionCaught event1 = localize.getEvent(1);
        assertSame(formatter, event1.getFormatter());
        assertSame(formatterException, event1.getException());
        assertSame(pattern.value(), event1.getRequest().getPattern());
        assertSame(request.getArguments(), event1.getRequest().getArguments());
    }
}
