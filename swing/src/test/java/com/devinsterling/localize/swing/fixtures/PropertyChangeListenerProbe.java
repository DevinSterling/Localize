package com.devinsterling.localize.swing.fixtures;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PropertyChangeListenerProbe implements PropertyChangeListener {
    public RuntimeException exception = new ListenerException();
    public final PropertyChangeListener delegate;
    public boolean throwOnCall;
    public int count;

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

    public PropertyChangeListenerProbe exception(RuntimeException exception) {
        this.exception = exception;
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
