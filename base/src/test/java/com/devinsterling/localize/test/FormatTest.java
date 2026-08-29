package com.devinsterling.localize.test;

import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FormatTest {

    @Test void testNamedArguments() {
        Localize localize = Localize.of();
        String value = localize.format("Hi, {first} {last}!")
                               .arg("last", "Doe")
                               .arg("first", "John")
                               .value();

        assertEquals("Hi, John Doe!", value);
    }

    @Test void testPositionalArguments() {
        Localize localize = Localize.of();
        String value = localize.format("Hi, {0} {1}!")
                               .arg("John")
                               .arg("Doe")
                               .value();

        assertEquals("Hi, John Doe!", value);
    }

    @Test void testNoArguments() {
        Localize localize = Localize.of();
        String value = localize.format("Hello world!").value();

        assertEquals("Hello world!", value);
    }
}
