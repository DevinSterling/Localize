package com.devinsterling.localize.event.impl;

import com.devinsterling.localize.event.EventListener;
import com.devinsterling.localize.event.LocalizeEvent;
import com.devinsterling.localize.event.Subscription;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventListenerRegistry {
    // Reads far exceeds writes
    private final CopyOnWriteArrayList<ListenerSubscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    public void fireEvent(LocalizeEvent event) {
        // NOTE: IF iterating over all subscriptions becomes a bottleneck in the future,
        //       a ConcurrentHashMap would be used as the backing data structure instead.
        for (ListenerSubscription<?> subscription : subscriptions) {
            subscription.invokeListener(event);
        }
    }

    public synchronized <T extends LocalizeEvent> Subscription addListener(
        Class<T> eventType,
        EventListener<? super T> listener
    ) {
        Objects.requireNonNull(listener, "listener must not be null");
        int index = find(eventType, listener);

        if (index >= 0) {
            return subscriptions.get(index);
        }

        ListenerSubscription<T> wrapper = new ListenerSubscription<>(eventType, listener);
        subscriptions.add(wrapper);
        return wrapper;
    }

    public synchronized <T extends LocalizeEvent> boolean removeListener(
        Class<T> eventType,
        EventListener<? super T> listener
    ) {
        Objects.requireNonNull(listener, "listener must not be null");
        int index = find(eventType, listener);

        if (index < 0) {
            return false;
        }

        subscriptions.remove(index).markDisposed();
        return true;
    }

    private <T extends LocalizeEvent> int find(Class<T> eventType, EventListener<? super T> listener) {
        int i = 0;

        for (ListenerSubscription<?> subscription : subscriptions) {
            if (subscription.eventType.equals(eventType) && subscription.listener.equals(listener)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    private class ListenerSubscription<T extends LocalizeEvent> implements Subscription {
        private final Class<T> eventType;
        private final EventListener<? super T> listener;
        private volatile boolean isActive = true;

        public ListenerSubscription(Class<T> eventType, EventListener<? super T> listener) {
            this.eventType = eventType;
            this.listener = listener;
        }

        private void markDisposed() {
            isActive = false;
        }

        @SuppressWarnings("unchecked")
        private void invokeListener(LocalizeEvent event) {
            if (eventType.isInstance(event)) {
                listener.onEvent((T) event);
            }
        }

        @Override public void dispose() {
            if (isActive) {
                markDisposed();
                subscriptions.remove(this);
            }
        }

        @Override public boolean isActive() {
            return isActive;
        }
    }
}
