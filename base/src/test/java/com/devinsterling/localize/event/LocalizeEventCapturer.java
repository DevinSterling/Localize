package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

final class LocalizeEventCapturer<T extends LocalizeEvent> extends Localize {
    private final List<T> capturedEvents = new ArrayList<>();
    private final Class<T> eventClass;

    public LocalizeEventCapturer(Class<T> eventClass) {
        super(Locale.ENGLISH, new LocalizeConfig());
        this.eventClass = eventClass;
    }

    protected void onEvent(LocalizeEvent event) {
        super.onEvent(event);

        if (eventClass.isInstance(event)) {
            capturedEvents.add(eventClass.cast(event));
        }
    }

    public int capturedEventsCount() {
        return capturedEvents.size();
    }

    public T getEvent(int i) {
        return getEvent(i, eventClass, LocalizeEvent.Cause.DIRECT);
    }

    public <U extends T> U getEvent(int i, Class<U> expectedEvent) {
        return getEvent(i, expectedEvent, LocalizeEvent.Cause.DIRECT);
    }

    public <U extends T> U getEvent(int i, Class<U> expectedEvent, LocalizeEvent.Cause cause) {
        T event = capturedEvents.get(i);

        assertSame(this, event.getSource());
        assertSame(cause, event.getCause());
        assertTrue(
            expectedEvent.isInstance(event),
        "Expected event at " + i + " should be " + expectedEvent.getSimpleName() +
                ", got " + event.getClass().getSimpleName()
        );

        return expectedEvent.cast(event);
    }
}
