package com.devinsterling.localize.test;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.LocalizationValueBuilder;
import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Test void testPositionalDeferredArgs() {
        Localize localize = getLocalizeInstance();
        AtomicInteger counter = new AtomicInteger();
        LocalizationValueBuilder<?> builder = localize.get(TEST_KEY_OUTPUT).arg(counter::get);

        assertEquals("Output: 0", builder.value());
        counter.incrementAndGet();
        counter.incrementAndGet();
        assertEquals("Output: 2", builder.value());
    }

    @Test void testNamedDeferredArgs() {
        Localize localize = getLocalizeInstance();
        AtomicInteger counter = new AtomicInteger();
        LocalizationValueBuilder<?> builder = localize.get(TEST_KEY_OUTPUT).arg("0", counter::get);

        counter.incrementAndGet();
        assertEquals("Output: 1", builder.value());
        counter.incrementAndGet();
        assertEquals("Output: 2", builder.value());
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
        Localize localize = Localize.of();
        assertThrows(NullPointerException.class, () -> new TestValueBuilder<>(null, localize));
    }

    @Test void testCustomBuilderNullLocalize() {
        assertThrows(
            NullPointerException.class,
            () -> {
                LocalizationRequestSource source = new LocalizationRequestSource.Key("key");
                new TestValueBuilder<>(source, null);
            }
        );
    }

    @Test void testCustomBuilder() {
        Localize localize = Localize.of();
        LocalizationRequestSource source = new LocalizationRequestSource.Key("key");
        TestValueBuilder<?> builder = new TestValueBuilder<>(source, localize);

        assertSame(builder, builder.defaultValue("test_default_value"));
        assertSame(builder, builder.args(Map.of("key1", "value1", "key2", "value2")));

        assertEquals(localize, builder.getLocalize());
        assertEquals(source, builder.getSource());
        assertEquals("test_default_value", builder.getDefaultValue());

        Arguments args = builder.arguments();
        assertEquals(Map.of("key1", "value1", "key2", "value2"), args.toNamedMap());
    }
}

class TestValueBuilder<B extends TestValueBuilder<B>> extends LocalizationValueBuilder<B> {

    TestValueBuilder(LocalizationRequestSource source, Localize localize) {
        super(source, localize);
    }

    /*//////////////////////////////////
    /// Protected method made public ///
    //////////////////////////////////*/

    public Localize getLocalize() {
        return super.getLocalize();
    }

    public LocalizationRequestSource getSource() {
        return super.getSource();
    }

    public String getDefaultValue() {
        return super.getDefaultValue();
    }

    public Arguments arguments() {
        return snapshotArguments();
    }
}
