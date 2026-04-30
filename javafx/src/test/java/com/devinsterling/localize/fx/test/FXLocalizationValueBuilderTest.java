package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.fx.LocalizeFX;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.function.Supplier;

import static com.devinsterling.localize.fx.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

class FXLocalizationValueBuilderTest {

    @Test void testGetBindingAgainstGet() {
        LocalizeFX localize = getLocalizeFXInstance();

        assertEquals(
            // JavaFX value builder
            localize.getBinding(() -> TEST_KEY_CLICK_ME).get(),
            // Plain value builder
            localize.get(() -> TEST_KEY_CLICK_ME).value()
        );
    }

    @Test void testBinding() {
        LocalizeFX localize = getLocalizeFXInstance();
        StringBinding binding = localize.get(TEST_KEY_CLICK_ME).binding();

        assertEquals("Click!", binding.get());
        localize.setLocale(Locale.JAPANESE);
        assertEquals("クリック！", binding.get());
        localize.setLocale(Locale.KOREAN);
        assertEquals("클릭!", binding.get());
    }

    @Test void testBindingWithArgs() {
        LocalizeFX localize = getLocalizeFXInstance();
        DoubleProperty clickCount = new SimpleDoubleProperty(1);
        StringProperty name = new SimpleStringProperty("John Doe");
        StringBinding binding = localize.get(TEST_KEY_CLICK_LABEL)
                                        .arg("click_count", clickCount)
                                        .arg("name", name)
                                        .binding();

        assertEquals("John Doe clicked this button one time!", binding.get());
        clickCount.set(2);
        assertEquals("John Doe clicked this button 2 times!", binding.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals("John Doeはこのボタンを2回クリックしました！", binding.get());
        name.set("Jane Doe");
        assertEquals("Jane Doeはこのボタンを2回クリックしました！", binding.get());
    }

    @Test void testBindingWithMixedArgs() {
        LocalizeFX localize = getLocalizeFXInstance();
        StringProperty name = new SimpleStringProperty("John Doe");
        StringBinding binding = localize.get(TEST_KEY_CLICK_LABEL)
                .arg("click_count", 0)
                .arg("name", name)
                .binding();

        assertEquals("John Doe clicked this button zero times!", binding.get());
        localize.setLocale(Locale.JAPANESE);
        assertEquals("John Doeはこのボタンを0回クリックしました！", binding.get());
        name.set("Jane Doe");
        localize.setLocale(Locale.KOREAN);
        assertEquals("Jane Doe이(가) 이 버튼을 0번 클릭했습니다!", binding.get());
        localize.setLocale(Locale.ENGLISH);
        assertEquals("Jane Doe clicked this button zero times!", binding.get());
    }

    @Test void testPlainValueWithArgs() {
        LocalizeFX localize = getLocalizeFXInstance();
        Supplier<String> supplier = () -> localize.get(TEST_KEY_CLICK_LABEL)
                .arg("name", "Doe")
                .arg("click_count", 777)
                .value();

        assertEquals("Doe clicked this button 777 times!", supplier.get());
        localize.setLocale(Locale.JAPANESE);
        assertEquals("Doeはこのボタンを777回クリックしました！", supplier.get());
        localize.setLocale(Locale.KOREAN);
        assertEquals("Doe이(가) 이 버튼을 777번 클릭했습니다!", supplier.get());
    }
}
