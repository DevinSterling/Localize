package com.devinsterling.localize.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class PropertyChangeManager {
    private Map<PropertyChangeListener, PropertyChangeSubscription> listeners;

    public Subscription addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return Subscription.EMPTY;
        }
        return listeners().computeIfAbsent(listener, PropertyChangeSubscription::new);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null || listeners == null) return;

        PropertyChangeSubscription subscription = listeners.remove(listener);

        if (subscription != null) {
            subscription.isActive = false;
        }
    }

    public void notifyChangeListeners(Object source, Locale oldLocale, Locale newLocale) {
        if (listeners == null) return;

        // Snapshot as listeners may remove themselves during iteration,
        // which can cause a `ConcurrentModificationException`.
        PropertyChangeListener[] snapshot = listeners.keySet().toArray(new PropertyChangeListener[0]);
        PropertyChangeEvent event = new PropertyChangeEvent(source, "locale", oldLocale, newLocale);

        for (PropertyChangeListener listener : snapshot) {
            listener.propertyChange(event);
        }
    }

    private Map<PropertyChangeListener, PropertyChangeSubscription> listeners() {
        return listeners == null ? listeners = new LinkedHashMap<>() : listeners;
    }

    private final class PropertyChangeSubscription implements Subscription {
        private final PropertyChangeListener listener;
        private boolean isActive = true;

        private PropertyChangeSubscription(PropertyChangeListener listener) {
            this.listener = listener;
        }

        @Override public void dispose() {
            if (isActive) {
                removePropertyChangeListener(listener);
            }
        }

        @Override public boolean isActive() {
            return isActive;
        }
    }
}
