package com.devinsterling.localize.test;

import com.devinsterling.localize.LocalizationValueBuilder;
import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.devinsterling.localize.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizationValueBuilderTest {

    @Test public void testBuilder() {
        Localize localize = getLocalizeInstance();

        assertEquals(
                localize.getValue(TEST_KEY_GREET),
                localize.get(TEST_KEY_GREET).value());
        assertEquals(
                localize.getValue(() -> TEST_KEY_TEST),
                localize.get(() -> TEST_KEY_TEST).value());
    }

    @Test public void testNamedArgs() {
        Localize localize = getLocalizeInstance();
        Supplier<String> supplier = () -> localize.get(TEST_KEY_NAMED)
                                                  .arg("first", "Apples")
                                                  .arg("last", "Strawberries")
                                                  .arg("ignored&missing_value", " missing ")
                                                  .args(Map.of("middle", "Oranges"))
                                                  .value();
        localize.setLocale(Locale.ENGLISH);
        assertEquals("Apples and Oranges and Strawberries", supplier.get());

        localize.setLocale(Locale.CHINESE);
        assertEquals("Apples和Oranges和Strawberries", supplier.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals("ApplesとOrangesとStrawberries", supplier.get());
    }

    @Test public void testNumberedArgs() {
        Localize localize = getLocalizeInstance();
        Supplier<String> supplier = () -> localize.get(TEST_KEY_NUMBERED)
                                                  .arg("Oranges") // argument 0
                                                  .args("Strawberries", "Apples") // argument 1, 2
                                                  .args() // No arguments passed
                                                  .arg(" missing ") // argument 3
                                                  .value();
        localize.setLocale(Locale.ENGLISH);
        assertEquals("Apples and Oranges and Strawberries", supplier.get());

        localize.setLocale(Locale.CHINESE);
        assertEquals("Apples和Oranges和Strawberries", supplier.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals("ApplesとOrangesとStrawberries", supplier.get());
    }

    @Test public void testExceptions() {
        Localize localize = Localize.of();

        assertDoesNotThrow(() -> localize.get("").arg("key", null));

        // Construction
        assertThrows(
                NullPointerException.class,
                () -> new LocalizationValueBuilder<>("", null));
        assertThrows(
                NullPointerException.class,
                () -> new LocalizationValueBuilder<>(null, request -> ""));

        // Adding arguments
        assertThrows(
                NullPointerException.class,
                () -> localize.get("").arg(null, "value"));
        assertThrows(
                IllegalStateException.class,
                () -> localize.get("").arg("key", "value").arg("value"));
        assertThrows(
                IllegalStateException.class,
                () -> localize.get("").args("key", "value").arg("key", "value"));
        assertThrows(
                IllegalStateException.class,
                () -> localize.get("").args("key", "value").args(Map.of("key", "value")));
    }
}
