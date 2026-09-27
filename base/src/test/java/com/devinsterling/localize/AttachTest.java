package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Locale;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class AttachTest {

    @Test void testBasicAttach() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize attached1 = new Localize(localize);
        Localize attached2 = new Localize(localize);

        assertNotEquals(localize, attached1);
        assertNotEquals(attached1, attached2);
        assertEquals(Locale.ENGLISH, attached1.getLocale());

        attached2.setLocale(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, attached2.getLocale());
        assertEquals(Locale.JAPANESE, localize.getLocale());

        assertEquals(localize.getProviderEntries(), attached1.getProviderEntries());
    }

    @Test void testNestedAttach() {
        Localize localize = Localize.of(Locale.ENGLISH);
        Localize attached1 = new Localize(localize);
        Localize attached2 = new Localize(attached1);

        assertEquals(Locale.ENGLISH, attached2.getLocale());

        localize.setLocale(Locale.CHINESE);
        assertEquals(Locale.CHINESE, attached1.getLocale());
        assertEquals(Locale.CHINESE, attached2.getLocale());
    }

    @SuppressWarnings({ "unused", "UnusedAssignment" })
    @Test void testAttachGarbageCollection() {
        Localize source = Localize.of(Locale.ENGLISH);
        WeakReference<Localize> weak = new WeakReference<>(new Localize(source));
        WeakReference<Localize> weakNested = new WeakReference<>(new Localize(weak.get()));

        awaitGarbageCollection();

        assertNull(weak.get());
        assertNull(weakNested.get());

        weak = new WeakReference<>(source);
        Localize attached = new Localize(source);
        source = null;

        awaitGarbageCollection();

        // Internally, attached holds a strong reference to its "parent" (`source`)
        // So the weak reference is not GC'd here
        assertNotNull(weak.get());
    }
}
