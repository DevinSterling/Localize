package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeEventTest {
    @Test void testCauses() {
        assertNotEquals(LocalizeEvent.Cause.DIRECT, LocalizeEvent.Cause.LOCALE_CHANGE);
        assertEquals("DIRECT", LocalizeEvent.Cause.DIRECT.toString());
        assertEquals("LOCALE_CHANGE", LocalizeEvent.Cause.LOCALE_CHANGE.toString());
    }

    @Test void setLocaleEventExceptionsAggregate() {
        Localize localize = getLocalizeInstance();
        AtomicInteger count = new AtomicInteger();
        RuntimeException exceptionA = new RuntimeException();
        RuntimeException exceptionB = new RuntimeException();

        Function<RuntimeException, EventListener<LocalizeEvent>> exceptionListener = r -> e -> {
            throw r;
        };
        EventListener<LocalizeEvent> listener = e -> count.incrementAndGet();

        localize.addListener(LocaleEvent.Replaced.class, exceptionListener.apply(exceptionA));
        localize.addListener(LocaleEvent.Replaced.class, listener);
        localize.addListener(ProviderEvent.Refreshed.class, exceptionListener.apply(exceptionB));
        localize.addListener(ProviderEvent.Refreshed.class, listener);

        var caught = assertThrows(RuntimeException.class, () -> localize.setLocale(Locale.JAPANESE));

        assertSame(caught, exceptionA);
        assertArrayEquals(new Throwable[] { exceptionB }, exceptionA.getSuppressed());
        assertEquals(2, count.get());
    }
}
