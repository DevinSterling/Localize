package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationFormatterTest {

    @Test void testFormatter() {
        Localize localize = getEmptyLocalizeInstance();
        String sample = "sample";
        LocalizationFormatter mock = (ctx) -> sample;

        assertEquals(LocalizationFormatter.STANDARD, localize.getFormatter());

        localize.setFormatter(mock);
        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.addProvider(TEST_PROVIDER);
        assertEquals(mock, localize.getFormatter());
        assertEquals(sample, localize.getValue(TEST_KEY_GREET));
        assertEquals(sample, localize.getValue(() -> TEST_KEY_TEST));

        localize.setFormatter(LocalizationFormatter.STANDARD);
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

    @Test void nestedFormatterCall() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.addProvider(locale -> key -> switch (key) {
            case "$outer$" -> "Hello $inner$!";
            case "$inner$" -> "world";
            default -> null;
        });
        localize.setFormatter(request -> {
            String nestedKey = "$inner$";
            String pattern = request.getPattern();

            if (pattern.contains(nestedKey)) {
                String nested = request.getLocalize().getValue(nestedKey);
                pattern = pattern.replace(nestedKey, nested);
            }

            return pattern;
        });

        assertEquals("Hello world!", localize.getValue("$outer$"));
    }

    @Test void formatterRequestPreservesOriginalSource() {
        Localize localize = Localize.of(Locale.ENGLISH);
        AtomicReference<LocalizationFormatter.Request> request = new AtomicReference<>();

        localize.addProvider(locale -> key -> "");
        localize.setFormatter(req -> {
            request.set(req);
            return LocalizationFormatter.STANDARD.format(req);
        });

        localize.getValue("A");
        assertEquals(
            new LocalizationRequestSource.Key("A"),
            request.get().getSource()
        );

        localize.format("Hi {user}!").value();
        assertEquals(
            new LocalizationRequestSource.Pattern("Hi {user}!"),
            request.get().getSource()
        );
    }
}
