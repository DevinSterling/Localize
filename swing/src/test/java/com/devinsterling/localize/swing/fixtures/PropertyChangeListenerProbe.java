package com.devinsterling.localize.swing.fixtures;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PropertyChangeListenerProbe implements PropertyChangeListener {
    public final ListenerException exception = new ListenerException();
    public final PropertyChangeListener delegate;
    public boolean throwOnCall;
    public int count = 0;

    public PropertyChangeListenerProbe() {
        this(event -> {});
    }

    public PropertyChangeListenerProbe(PropertyChangeListener delegate) {
        this.delegate = delegate;
    }

    public PropertyChangeListenerProbe throwOnCall(boolean throwOnCall) {
        this.throwOnCall = throwOnCall;
        return this;
    }

    @Override public void propertyChange(PropertyChangeEvent event) {
        if (throwOnCall) {
            throw exception;
        }
        delegate.propertyChange(event);
        count++;
    }

    public static class ListenerException extends RuntimeException {}
}
