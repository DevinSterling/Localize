package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessorTest {

    @Test void testProcessor() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String sample = "sample";
        LocalizationRequestProcessor mock = (ctx) -> sample;

        assertEquals(LocalizationRequestProcessor.DEFAULT, localize.getProcessor());

        localize.setProcessor(mock);
        // No bundles contained
        assertEquals("", localize.getValue(TEST_KEY_GREET));

        localize.putBundleProvider("key", TEST_PROVIDER);
        assertEquals(mock, localize.getProcessor());
        assertEquals(sample, localize.getValue(TEST_KEY_GREET));
        assertEquals(sample, localize.getValue(() -> TEST_KEY_TEST));

        localize.setProcessor(LocalizationRequestProcessor.DEFAULT);
        assertEquals("hi", localize.getValue(TEST_KEY_GREET));
        assertEquals("test", localize.getValue(() -> TEST_KEY_TEST));
    }

    @Test void testPositionalArgumentsAreStoredAsNamed() {
        Localize localize = Localize.of();
        LocalizationRequestProcessor defaultProcessor = localize.getProcessor();
        LocalizationRequestProcessor testProcessor = new LocalizationRequestProcessor() {
            @Override public String process(Context context) {
                Arguments arguments = context.getArguments();

                // Not backed by a list
                assertNotSame(arguments.toList(), arguments.toList());

                // Backed by a map (returns an unmodifiable view)
                assertSame(arguments.toNamedMap(), arguments.toNamedMap());

                assertEquals(Arguments.Type.POSITIONAL, arguments.type());
                assertEquals(List.of(0, 1, 2, 3), arguments.values().stream().toList());

                return defaultProcessor.process(context);
            }

            @Override public Arguments.Type argumentsHint() {
                return Arguments.Type.NAMED;
            }
        };

        localize.setProcessor(testProcessor);
        localize.get(TEST_KEY_NUMBERED).args(0, 1, 2, 3).value();
    }

    @Test void testPositionalArgumentsStoredAsNamedDisallowMixed() {
        Localize localize = Localize.of();
        LocalizationRequestProcessor defaultProcessor = localize.getProcessor();
        LocalizationRequestProcessor testProcessor = new LocalizationRequestProcessor() {
            @Override public String process(Context context) {
                return defaultProcessor.process(context);
            }

            @Override public Arguments.Type argumentsHint() {
                return Arguments.Type.NAMED;
            }
        };

        localize.setProcessor(testProcessor);
        assertThrows(
            IllegalStateException.class,
            () -> localize.get(TEST_KEY_NUMBERED).args(0).arg("mixed", 1)
        );
    }
}
