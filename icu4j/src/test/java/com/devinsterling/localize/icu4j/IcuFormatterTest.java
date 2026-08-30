package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.LocalizationValueBuilder;
import com.devinsterling.localize.Localize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.devinsterling.localize.icu4j.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class IcuFormatterTest {

    @Test void testDefaultIcuFormatterConfig(){
        IcuFormatter formatter = new IcuFormatter();

        assertEquals(new IcuFormatterConfig(), formatter.getConfig());
    }

    @Test void testDefaultIsIcuFormatter(){
        Localize localize = Localize.of();
        LocalizationFormatter formatter = localize.getFormatter();

        assertInstanceOf(IcuFormatter.class, formatter);
    }

    @Test void testArgumentHintIsNamed(){
        Localize localize = Localize.of();
        LocalizationFormatter formatter = localize.getFormatter();

        assertEquals(Arguments.Type.NAMED, formatter.argumentsHint());
    }

    @Test void testNoKeyFound() {
        Localize localize = getMessageFormat2Instance();
        localize.getConfig().setDefaultMissingValue("?");

        assertEquals("?", localize.getValue("non-existent"));
    }

    @Test void testSetFormatterDuringRuntime() {
        Localize localize = Localize.of(Locale.ENGLISH);
        localize.putBundleProvider("main", MESSAGE_FORMAT2);
        IcuFormatter formatter = (IcuFormatter) localize.getFormatter();

        LocalizationValueBuilder<?> builder = localize.get(TEST_KEY_CLICK_LABEL)
                .arg("name", "Doe")
                .arg("click_count", 1337);

        // The default formatter is ICU4J's message formatter 2
        assertEquals(IcuMessageFormat.MESSAGE2_FORMAT, formatter.getConfig().getMessageFormat());
        assertEquals("Doe clicked this button 1,337 times!", builder.value());

        // Change the resource bundle provider as the syntax between ICU4J's formatters change
        localize.putBundleProvider("main", MESSAGE_FORMAT1);
        formatter.getConfig().setFormatter(IcuMessageFormat.MESSAGE1_FORMAT);

        assertEquals("Doe clicked this button 1,337 times!", builder.value());
    }

    @MethodSource("icuFormatters")
    @ParameterizedTest void testIcuMessageFormatNoArgs(Localize localize) {
        assertEquals("Click!", localize.getValue(TEST_KEY_CLICK_ME));

        localize.setLocale(Locale.JAPANESE);
        assertEquals("クリック！", localize.getValue(TEST_KEY_CLICK_ME));
    }

    @MethodSource("icuFormatters")
    @ParameterizedTest void testIcuFormatterWithArgs(Localize localize) {
        AtomicInteger clickCount = new AtomicInteger(0);
        AtomicReference<String> name = new AtomicReference<>("Snowball");

        LocalizationValueBuilder<?> builder = localize.get(TEST_KEY_CLICK_LABEL)
                .arg("click_count", clickCount::getAndIncrement)
                .arg("name", name::get);

        assertEquals("Snowball did not click this button!", builder.value());
        assertEquals("Snowball clicked this button one time!", builder.value());
        assertEquals("Snowball clicked this button 2 times!", builder.value());

        name.set("Blue");
        assertEquals("Blue clicked this button 3 times!", builder.value());

        clickCount.set(0);
        localize.setLocale(Locale.JAPANESE);
        assertEquals("Blueはこのボタンを0回クリックしました！", builder.value());

        name.set("山田花子");
        assertEquals("山田花子はこのボタンを1回クリックしました！", builder.value());
        assertEquals("山田花子はこのボタンを2回クリックしました！", builder.value());
    }

    static Stream<Localize> icuFormatters() {
        return Stream.of(
            getMessageFormat1Instance(),
            getMessageFormat2Instance()
        );
    }
}
