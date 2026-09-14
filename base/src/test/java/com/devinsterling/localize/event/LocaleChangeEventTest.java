package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class LocaleChangeEventTest {

    @Test void testOnLocaleChange() {
        LocalizeEventTest localize = new  LocalizeEventTest(Locale.ENGLISH);

        LocaleChangeEvent event1 = localize.setLocaleAndGetEvent(Locale.CHINESE);
        assertEquals(Locale.ENGLISH, event1.getOldLocale());
        assertEquals(Locale.CHINESE, event1.getNewLocale());
        assertTrue(event1.isValid());

        LocaleChangeEvent event2 = localize.setLocaleAndGetEvent(Locale.JAPANESE);
        assertEquals(Locale.CHINESE, event2.getOldLocale());
        assertEquals(Locale.JAPANESE, event2.getNewLocale());
        assertTrue(event2.isValid());
        assertFalse(event1.isValid());

        // Despite the previous test being returned, no new event is fired as the locale is the same
        LocaleChangeEvent event3 = localize.setLocaleAndGetEvent(Locale.JAPANESE);
        assertSame(event2, event3);

        assertEquals(2, localize.localeChangeEventCount);
    }

    private static final class LocalizeEventTest extends Localize {
        private int localeChangeEventCount = 0;
        private LocaleChangeEvent recentLocaleChangeEvent;

        private LocalizeEventTest(Locale locale) {
            super(locale, new LocalizeConfig());
        }

        @Override protected void onLocaleChanged(LocaleChangeEvent event) {
            recentLocaleChangeEvent = event;
            localeChangeEventCount++;
        }

        public LocaleChangeEvent setLocaleAndGetEvent(Locale locale) {
            setLocale(locale);
            assertSame(this, recentLocaleChangeEvent.getSource());
            assertSame(LocalizeEvent.Cause.EXTERNAL, recentLocaleChangeEvent.getCause());
            return recentLocaleChangeEvent;
        }
    }
}
