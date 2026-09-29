package com.devinsterling.localize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FormatTest {

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
        String value = localize.format("{x} {y} {z}")
                .arg("x", 0)
                .value();

        assertEquals("0 {1} {2}", value);
    }

    @Test void testUnescapeQuotes() {
        Localize localize = Localize.of(Locale.ENGLISH);
        String value = localize.format("it''s, it''d").value();

        assertEquals("it's, it'd", value);
    }

    @MethodSource("formatFailCases")
    @ParameterizedTest void testUnmatchedNamedBrace(FailCase testCase) {
        Localize localize = Localize.of(Locale.ENGLISH);
        LocalizationValueBuilder<?> builder = localize.format(testCase.pattern).args(testCase.arguments);

        // An exception will be thrown since the brace for `y` is not closed
        assertThrows(IllegalArgumentException.class, builder::value);
    }

    static Stream<FailCase> formatFailCases() {
        return Stream.of(
            new FailCase("{y {x}", Map.of("x", 0, "y", 1)),
            new FailCase("{x} {y", Map.of("x", 0, "y", 1)),
            new FailCase("{x{y}}", Map.of("x", 0, "y", 1)),
            new FailCase("{x, number, {yx}", Map.of("x", 0))
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
            new Case("{x} '''{ignored}''' {x}", "1 '{ignored}' 1", Map.of("x", 1)),
            new Case("{x} '{ignored}' {x}", "2 {ignored} 2", Map.of("x", 2)),
            new Case("{x} '{ignored} {x}'", "3 {ignored} {x}", Map.of("x", 3)),
            new Case("{x} '{ignored} {x}", "4 {ignored} {x}", Map.of("x", 4)),
            new Case("{x} '{x", "5 {x", Map.of("x", 5)),
            new Case("'{x}' {x}", "{x} 6", Map.of("x", 6)),
            new Case("{x} {x, number} '{x, number}'", "7 7 {x, number}", Map.of("x", 7)),
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
