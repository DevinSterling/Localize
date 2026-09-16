package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class CompositionTest {

    @Test void testBasicComposition() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize wrapped1 = new Localize(localize);
        Localize wrapped2 = new Localize(localize);

        assertNotEquals(localize, wrapped1);
        assertNotEquals(wrapped1, wrapped2);
        assertEquals(Locale.ENGLISH, wrapped1.getLocale());

        wrapped2.setLocale(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, wrapped2.getLocale());
        assertEquals(Locale.JAPANESE, localize.getLocale());

        assertEquals(localize.getProviderEntries(), wrapped1.getProviderEntries());
    }

    @Test void testNestedComposition() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize wrapped1 = new Localize(localize);
        Localize wrapped2 = new Localize(wrapped1);

        assertEquals(Locale.ENGLISH, wrapped2.getLocale());

        localize.setLocale(Locale.CHINESE);
        assertEquals(Locale.CHINESE, wrapped1.getLocale());
        assertEquals(Locale.CHINESE, wrapped2.getLocale());
    }

    @Test void testParents() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize wrapped1A = new Localize(localize);
        Localize wrapped1B = new Localize(localize);
        Localize wrapped2 = new Localize(wrapped1A);


    }

    @Test void testDelegation() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize wrapped1 = new Localize(localize);
        Localize wrapped2 = new Localize(wrapped1);
    }
}
