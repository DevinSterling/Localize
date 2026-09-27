package com.devinsterling.localize.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeEventTest {
    @Test void testCauses() {
        assertNotEquals(LocalizeEvent.Cause.DIRECT, LocalizeEvent.Cause.LOCALE_CHANGE);
        assertEquals("DIRECT", LocalizeEvent.Cause.DIRECT.toString());
        assertEquals("LOCALE_CHANGE", LocalizeEvent.Cause.LOCALE_CHANGE.toString());
    }
}
