package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class MissingValueTest {

    @Test void missingValueExceptionFieldAccess() {
        Localize localize = Localize.of(Locale.ENGLISH);
        LocalizationRequest request = LocalizationRequest
                .Builder
                .of(new LocalizationRequestSource.Key("abc"))
                .build();

        localize.getConfig().setThrowOnMissingValue(true);

        var caught = assertThrows(MissingValueException.class, () -> localize.formatValue(request));
        assertEquals(localize, caught.getLocalize());
        assertEquals(request, caught.getRequest());
        assertNull(caught.getCause());
    }

    @Test void missingPatternForKey() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.addProvider(locale -> key -> null);

        assertEquals("[abc]", localize.getValue("abc"));

        localize.getConfig().setThrowOnMissingValue(true);

        var caught = assertThrows(MissingValueException.class, () -> localize.getValue("abc"));
        assertEquals("Cannot find pattern for key: abc", caught.getMessage());
        assertNull(caught.getCause());
    }

    @Test void missingValueExceptionForKey() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.setFormatter(request -> null);
        localize.addProvider(locale -> key -> "{pattern}");

        assertEquals("[abc]", localize.getValue("abc"));

        localize.getConfig().setThrowOnMissingValue(true);

        var caught = assertThrows(MissingValueException.class, () -> localize.getValue("abc"));
        assertEquals("Formatter returned `null` for key: abc", caught.getMessage());
        assertNull(caught.getCause());

        localize.getConfig().setIgnoreFormatterExceptions(true);
        UnsupportedOperationException exception = new UnsupportedOperationException();
        localize.setFormatter(request -> {
            throw exception;
        });

        caught = assertThrows(MissingValueException.class, () -> localize.getValue("abc"));
        assertEquals("Formatter threw an exception for key: abc", caught.getMessage());
        assertEquals(exception, caught.getCause());
    }

    @Test void missingValueExceptionForPattern() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.setFormatter(request -> null);

        assertEquals("{xyz}", localize.format("{xyz}").value());

        localize.getConfig().setThrowOnMissingValue(true);

        var caught = assertThrows(MissingValueException.class, () -> localize.format("{xyz}").value());
        assertEquals("Formatter returned `null` for pattern", caught.getMessage());
        assertNull(caught.getCause());

        localize.getConfig().setIgnoreFormatterExceptions(true);
        UnsupportedOperationException exception = new UnsupportedOperationException();
        localize.setFormatter(request -> {
            throw exception;
        });

        caught = assertThrows(MissingValueException.class, () -> localize.format("{xyz}").value());
        assertEquals("Formatter threw an exception for pattern", caught.getMessage());
        assertEquals(exception, caught.getCause());
    }
}
