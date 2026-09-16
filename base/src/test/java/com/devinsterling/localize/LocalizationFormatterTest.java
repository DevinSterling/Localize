package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizationFormatterTest {

    @Test void testFormatter() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String sample = "sample";
        LocalizationFormatter mock = (ctx) -> sample;

        assertEquals(LocalizationFormatter.DEFAULT, localize.getFormatter());

        localize.setFormatter(mock);
        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.putProvider("key", TEST_PROVIDER);
        assertEquals(mock, localize.getFormatter());
        assertEquals(sample, localize.getValue(TEST_KEY_GREET));
        assertEquals(sample, localize.getValue(() -> TEST_KEY_TEST));

        localize.setFormatter(LocalizationFormatter.DEFAULT);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
        assertEquals("test", localize.getValue(() -> TEST_KEY_TEST));
    }

    @Test void testPositionalArgumentsAreStoredAsNamed() {
        Localize localize = Localize.of();
        LocalizationFormatter defaultFormatter = localize.getFormatter();
        LocalizationFormatter testFormatter = new LocalizationFormatter() {
            @Override public String format(Request request) {
                Arguments arguments = request.getArguments();

                // Not backed by a list
                assertNotSame(arguments.toList(), arguments.toList());

                // Backed by a map (returns an unmodifiable view)
                assertSame(arguments.toNamedMap(), arguments.toNamedMap());

                assertEquals(Arguments.Type.POSITIONAL, arguments.type());
                assertEquals(List.of(0, 1, 2, 3), arguments.values().stream().toList());

                return defaultFormatter.format(request);
            }

            @Override public Arguments.Type argumentsHint() {
                return Arguments.Type.NAMED;
            }
        };

        localize.setFormatter(testFormatter);
        localize.get(TEST_KEY_NUMBERED).args(0, 1, 2, 3).value();
    }

    @Test void testPositionalArgumentsStoredAsNamedDisallowMixed() {
        Localize localize = Localize.of();
        LocalizationFormatter defaultFormatter = localize.getFormatter();
        LocalizationFormatter testFormatter = new LocalizationFormatter() {
            @Override public String format(Request request) {
                return defaultFormatter.format(request);
            }

            @Override public Arguments.Type argumentsHint() {
                return Arguments.Type.NAMED;
            }
        };

        localize.setFormatter(testFormatter);
        assertThrows(
            IllegalStateException.class,
            () -> localize.get(TEST_KEY_NUMBERED).args(0).arg("mixed", 1)
        );
    }
}
