package com.devinsterling.localize.fx.test;

import com.devinsterling.localize.fx.LocalizeFX;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.devinsterling.localize.fx.test.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class FXLocalizationValueBuilderTest {

    @Test public void testBinding() {
        LocalizeFX localize = getLocalizeFXInstance();
        StringBinding binding = localize.get(TEST_KEY_CLICK_ME).binding();

        assertEquals("Click!", binding.get());
        localize.setLocale(Locale.JAPANESE);
        assertEquals("クリック！", binding.get());
    }

    @Test public void testBindingWithArgs() {
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
}
