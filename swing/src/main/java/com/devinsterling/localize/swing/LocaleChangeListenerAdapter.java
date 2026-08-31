package com.devinsterling.localize.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

final class LocaleChangeListenerAdapter implements PropertyChangeListener {
    private final LocaleChangeListener listener;

    public LocaleChangeListenerAdapter(LocaleChangeListener listener) {
        this.listener = listener;
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();

        if (oldValue instanceof Locale oldLocale && newValue instanceof Locale newLocale) {
            listener.onChange(oldLocale, newLocale);
        } else {
            // This scenario never occurs because this API is private/internal
            throw new IllegalStateException("The given property values are not of type " + Locale.class.getName());
        }
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof LocaleChangeListenerAdapter other && listener.equals(other.listener);
    }

    @Override public int hashCode() {
        return listener.hashCode();
    }
}
