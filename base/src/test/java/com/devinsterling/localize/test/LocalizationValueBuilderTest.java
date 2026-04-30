package com.devinsterling.localize.test;

import com.devinsterling.localize.LocalizationValueBuilder;
import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.devinsterling.localize.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationValueBuilderTest {

    @Test void testBuilder() {
        Localize localize = getLocalizeInstance();

        assertEquals(
                localize.getValue(TEST_KEY_GREET),
                localize.get(TEST_KEY_GREET).value());
        assertEquals(
                localize.getValue(() -> TEST_KEY_TEST),
                localize.get(() -> TEST_KEY_TEST).value());
    }

    @Test void testNamedArgs() {
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

    @Test void testNumberedArgs() {
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

    @Test void testDuplicateArgs() {
        Localize localize = getLocalizeInstance();

        localize.setLocale(Locale.JAPANESE);
        String value = localize.get(TEST_KEY_NAMED)
                               .arg("first", "Apples")
                               .arg("first", "Pears")
                               .arg("middle", "Oranges")
                               .arg("last", "Mangoes")
                               .arg("last", "Strawberries")
                               .arg("last", "Bananas")
                               .value();

        assertEquals("PearsとOrangesとBananas", value);
    }

    @Test void testDefaultValue() {
        String defaultValue = "default";
        Localize localize = getLocalizeInstance();

        // By default, if a key is not found, an empty string is returned
        assertEquals("", localize.get("doesn't exist").value());
        // The key doesn't exist, so the default value specified is returned
        assertEquals(defaultValue, localize.get("doesn't exist").defaultValue(defaultValue).value());
        // The key exists, so the default value is not returned
        assertEquals("hi", localize.get(TEST_KEY_GREET).defaultValue(defaultValue).value());
    }

    @Test void testExceptionsFromArguments() {
        Localize localize = Localize.of();

        assertDoesNotThrow(() -> localize.get("").arg("key", null));

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

    @Test void testCustomBuilderNullKey() {
        LocalizationValueBuilder.Applier mockApplier = _ignoredRequest -> "";
        assertThrows(NullPointerException.class, () -> new TestValueBuilder<>(null, mockApplier));
    }

    @Test void testCustomBuilderNullApplier() {
        assertThrows(NullPointerException.class, () -> new TestValueBuilder<>("key", null));
    }

    @Test void testCustomBuilder() {
        LocalizationValueBuilder.Applier mockApplier = _ignoredRequest -> "";
        TestValueBuilder<?> builder = new TestValueBuilder<>("key", mockApplier);

        assertSame(builder, builder.defaultValue("test_default_value"));
        assertSame(builder, builder.args(Map.of("key1", "value1", "key2", "value2")));

        assertEquals(mockApplier, builder.getApplier());
        assertEquals("key", builder.getKey());
        assertEquals("test_default_value", builder.getDefaultValue());
        assertEquals(Map.of("key1", "value1", "key2", "value2"), builder.getArguments());
    }
}

class TestValueBuilder<B extends TestValueBuilder<B>> extends LocalizationValueBuilder<B> {

    TestValueBuilder(String key, Applier applier) {
        super(key, applier);
    }

    /*//////////////////////////////////
    /// Protected method made ///
    //////////////////////////////////*/

    public Applier getApplier() {
        return super.getApplier();
    }

    public String getKey() {
        return super.getKey();
    }

    public String getDefaultValue() {
        return super.getDefaultValue();
    }

    public Map<String, Object> getArguments() {
        return super.getArguments();
    }
}
