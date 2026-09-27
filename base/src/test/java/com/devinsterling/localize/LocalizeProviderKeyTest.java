package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeProviderKeyTest {

    @Test void testUniqueKey() {
        Localize.ProviderKey a = Localize.ProviderKey.of();
        Localize.ProviderKey b = Localize.ProviderKey.of();

        assertNotEquals(a, b);
    }

    @Test void testStringKey() {
        Localize.ProviderKey a = Localize.ProviderKey.of("foo");
        Localize.ProviderKey b = Localize.ProviderKey.of("foo");
        Localize.ProviderKey c = Localize.ProviderKey.of("bar");

        assertEquals(a, b);
        assertNotEquals(c, b);
    }

    @Test void testStringKeyToString() {
        Localize.ProviderKey a = Localize.ProviderKey.of("foo");
        Localize.ProviderKey b = Localize.ProviderKey.of("bar");

        assertEquals("foo", a.toString());
        assertEquals("bar", b.toString());
    }
}
