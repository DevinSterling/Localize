package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class LocaleEventTest {

    @Test void testOnLocaleChange() {
        LocalizeEventTest localize = new  LocalizeEventTest(Locale.ENGLISH);

        LocaleEvent.Replaced event1 = localize.setLocaleAndGetEvent(Locale.CHINESE);
        assertEquals(Locale.ENGLISH, event1.getOldLocale());
        assertEquals(Locale.CHINESE, event1.getNewLocale());
        assertTrue(event1.isValid());

        LocaleEvent.Replaced event2 = localize.setLocaleAndGetEvent(Locale.JAPANESE);
        assertEquals(Locale.CHINESE, event2.getOldLocale());
        assertEquals(Locale.JAPANESE, event2.getNewLocale());
        assertTrue(event2.isValid());
        assertFalse(event1.isValid());

        // Despite the previous test being returned, no new event is fired as the locale is the same
        LocaleEvent event3 = localize.setLocaleAndGetEvent(Locale.JAPANESE);
        assertSame(event2, event3);

        assertEquals(2, localize.localeChangeEventCount);
    }

    private static final class LocalizeEventTest extends Localize {
        private int localeChangeEventCount = 0;
        private LocaleEvent.Replaced recentLocaleEvent;

        private LocalizeEventTest(Locale locale) {
            super(locale, new LocalizeConfig());
        }

        @Override protected void onLocaleReplaced(LocaleEvent.Replaced event) {
            recentLocaleEvent = event;
            localeChangeEventCount++;
        }

        public LocaleEvent.Replaced setLocaleAndGetEvent(Locale locale) {
            setLocale(locale);
            assertSame(this, recentLocaleEvent.getSource());
            assertSame(LocalizeEvent.Cause.DIRECT, recentLocaleEvent.getCause());
            return recentLocaleEvent;
        }
    }
}
