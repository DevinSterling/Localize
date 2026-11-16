package com.devinsterling.localize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {

    @Test void testNamedArguments() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("Hi, {first} {last}!")
                               .arg("last", "Doe")
                               .arg("first", "John")
                               .value();

        assertEquals("Hi, John Doe!", value);
    }

    @Test void testPositionalArguments() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("Hi, {0} {1}!")
                               .arg("John")
                               .arg("Doe")
                               .value();

        assertEquals("Hi, John Doe!", value);
    }

    @Test void testNoArguments() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("Hello world!").value();

        assertEquals("Hello world!", value);
    }

    @Test void testMissingNamedArguments() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("{x} {y, number, 0.00} {z}")
                .arg("x", 0)
                .value();

        assertEquals("0 {y} {z}", value);
    }

    @Test void testUnescapeQuotes() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("it''s, it''d").value();

        assertEquals("it's, it'd", value);
    }

    @MethodSource("formatFailCases")
    @ParameterizedTest void testNamedArgumentsThrows(FailCase testCase) {
        Localize localize = Localize.of(Locale.ENGLISH);
        LocalizationValueBuilder<?> builder = localize.format(testCase.pattern).args(testCase.arguments);

        assertThrows(IllegalArgumentException.class, builder::value);
    }

    static Stream<FailCase> formatFailCases() {
        return Stream.of(
            // Blank
            new FailCase("{} {x}", Map.of("x", 0)),
            new FailCase("{x} {  }", Map.of("x", 0)),
            // First brace unclosed
            new FailCase("{y {x}", Map.of("x", 0, "y", 1)),
            // Second brace unclosed
            new FailCase("{x} {y", Map.of("x", 0, "y", 1)),
            // Nested braces
            new FailCase("{x{y}}", Map.of("x", 0, "y", 1)),
            // First brace unclosed for sub-pattern
            new FailCase("{x, number, {yx}", Map.of("x", 0)),
            // Unclosed skipped element
            new FailCase("{m, choice, 0#x", Map.of("x", 1)),
            // Inner brace unclosed
            new FailCase("{m, choice, 0#{x}", Map.of("x", 1)),
            // Stray quote in skipped element
            new FailCase("{m, choice, 0# ' {x}} z", Map.of("x", 1))
        );
    }

    @MethodSource("formatSuccessCases")
    @ParameterizedTest void testNamedArguments(Case testCase) {
        Localize localize = Localize.of();
        String value = localize.format(testCase.pattern).args(testCase.arguments).value();

        assertEquals(testCase.expected, value);
    }

    static Stream<Case> formatSuccessCases() {
        return Stream.of(
            new Case("it''s {missing}", "it's {missing}", Map.of()),
            new Case("{missing} {x}", "{missing} 1", Map.of("x", 1)),
            // Quoting
            new Case("{x} '''{ignored}''' {x}", "1 '{ignored}' 1", Map.of("x", 1)),
            new Case("{x} '{ignored}' {x}", "2 {ignored} 2", Map.of("x", 2)),
            new Case("{x} '{ignored} {x}'", "3 {ignored} {x}", Map.of("x", 3)),
            new Case("{x} '{ignored} {x}", "4 {ignored} {x}", Map.of("x", 4)),
            new Case("{x} '{x", "5 {x", Map.of("x", 5)),
            // Escaped argument
            new Case("'{x}' {x}", "{x} 6", Map.of("x", 6)),
            // Missing argument within sub-pattern
            new Case("{x, choice, 0<{missing} y}", "{missing} y", Map.of("x", 1)),
            // Missing typed element nested inside a present choice branch
            new Case("{x, choice, 1<{missing, choice, 0#a} y}", "{missing} y", Map.of("x", 2)),
            // Collapse dangling sup-pattern with an apostrophe and a nested key inside it
            new Case("{missing, choice, 0#it''s {x}} y", "{missing} y", Map.of("x", 1)),
            // Collapse dangling sup-pattern with nested quoted brace
            new Case("{missing, choice, 0#'}'} y", "{missing} y", Map.of("x", 1)),
            // Repeated missing key, simple and typed, sharing one slot
            new Case("{z} {z, number}", "{z} {z}", Map.of("x", 1)),
            new Case("{x} {x, number} '{x, number}'", "7 7 {x, number}", Map.of("x", 7)),
            // Mixed
            new Case(
                "{x} {y} '{x} {y}' '{z}' a={a, number,0.00}",
                "Doe 1,234 {x} {y} {z} a=9876.50",
                Map.of("x", "Doe", "y", 1234, "z", "hello", "a", 9876.50)
            )
        );
    }

    private record Case(String pattern, String expected, Map<String, Object> arguments) {}
    private record FailCase(String pattern, Map<String, Object> arguments) {}
}
