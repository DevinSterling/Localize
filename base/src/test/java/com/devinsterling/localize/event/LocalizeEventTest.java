package com.devinsterling.localize.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeEventTest {
    @Test void testCauses() {
        assertNotEquals(LocalizeEvent.Cause.EXTERNAL, LocalizeEvent.Cause.LOCALE_CHANGE);
        assertEquals("EXTERNAL", LocalizeEvent.Cause.EXTERNAL.toString());
        assertEquals("LOCALE_CHANGE", LocalizeEvent.Cause.LOCALE_CHANGE.toString());
    }
}
