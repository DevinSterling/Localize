package com.devinsterling.localize.swing;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import static com.devinsterling.localize.swing.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class SwingEdtDispatchTest {

    @Test public void testLocaleChangeDispatchesToEdt() throws InterruptedException, InvocationTargetException {
        LocalizeSwing localize = getLocalizeSwingInstance();
        JLabel label = new JLabel();

        localize.bind(TEST_KEY_CLICK_ME, label);

        localize.setLocale(Locale.JAPANESE);
        localize.setLocale(Locale.KOREAN);

        SwingUtilities.invokeAndWait(() -> assertEquals("클릭!", label.getText()));
    }
}
