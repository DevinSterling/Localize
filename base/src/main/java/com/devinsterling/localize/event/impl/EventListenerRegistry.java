package com.devinsterling.localize.event.impl;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.EventListener;
import com.devinsterling.localize.event.LocalizeEvent;
import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.internal.ExceptionHelper;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventListenerRegistry {
    /// All [Localize] instances attached to this registry.
    /// Attached instances take priority over registered external listeners.
    private final CopyOnWriteArrayList<LocalizeOnEventHook> attached = new CopyOnWriteArrayList<>();
    /// Subscriptions for external listeners.
    ///
    /// Reads far exceeds writes.
    /// If the current list-based approach ever becomes a **bottleneck**
    /// (e.g., hundreds of listeners), a suitable replacement is:
    /// ```
    /// Map<Class<? extends LocalizeEvent>, List<ListenerSubscription<?>>>
    /// ```
    private final CopyOnWriteArrayList<ListenerSubscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    public LocalizeOnEventHook attach(OnEventHook onEventHook, Localize localize) {
        LocalizeOnEventHook hook = new LocalizeOnEventHook(onEventHook, localize);
        attached.add(hook);
        return hook;
    }

    public void detach(LocalizeOnEventHook hook) {
        attached.remove(hook);
    }

    public void fireEventToHook(LocalizeOnEventHook hook, LocalizeEvent event) {
        Localize instance = hook.reference.get();

        try {
            hook.onEvent(event);
        } catch (RuntimeException e) {
            RuntimeException caught = reportException(null, hook, instance, event, e);

            if (caught != null) {
                throw caught;
            }
        }
    }

    public void fireEvent(Localize source, LocalizeEvent event) {
        RuntimeException aggregate = null;
        boolean needsCleanup = false;

        for (LocalizeOnEventHook hook : attached) {
            Localize instance = hook.reference.get();

            if (instance != null) {
                try {
                    hook.onEvent(event);
                } catch (RuntimeException e) {
                    aggregate = reportException(aggregate, hook, source, event, e);
                }
            } else {
                needsCleanup = true;
            }
        }

        if (needsCleanup) {
            attached.removeIf(LocalizeOnEventHook::isGarbageCollected);
        }

        for (ListenerSubscription<?> subscription : subscriptions) try {
            subscription.invokeListener(event);
        } catch (RuntimeException e) {
            aggregate = reportException(aggregate, subscription.listener, source, event, e);
        }

        if (aggregate != null) {
            throw aggregate;
        }
    }

    private RuntimeException reportException(
        RuntimeException aggregate,
        EventListener<?> listener,
        Localize source,
        LocalizeEvent event,
        RuntimeException exception
    ) {
        aggregate = aggregate(source, aggregate, exception);

        // If the given event is an instance of `ListenerExceptionCaught`, avoid recursive handling.
        if (!(event instanceof DiagnosticEvent.ListenerExceptionCaught)) {
            try {
                fireEvent(
                    source,
                    new EventImpls.Diagnostic.ListenerExceptionCaught(source, listener, event, exception)
                );
            } catch (RuntimeException e) {
                aggregate = aggregate(source, aggregate, e);
            }
        }

        return aggregate;
    }

    // Aggregate exceptions from listeners.
    // One listener should not prevent all other listeners, especially `onEvent` hooks.
    private static RuntimeException aggregate(
        Localize source,
        RuntimeException aggregate,
        RuntimeException exception
    ) {
        if (source.getConfig().isIgnoreListenerExceptions()) {
            return aggregate;
        } else {
            return ExceptionHelper.aggregate(aggregate, exception);
        }
    }

    public synchronized <T extends LocalizeEvent> Subscription addListener(
        Class<T> eventType,
        EventListener<? super T> listener
    ) {
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
        int index = find(eventType, listener);

        if (index < 0) {
            return false;
        }

        subscriptions.remove(index).markDisposed();
        return true;
    }

    private <T extends LocalizeEvent> int find(Class<T> eventType, EventListener<? super T> listener) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(listener, "listener must not be null");

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
            if (eventType.isInstance(event) && isActive) {
                listener.onEvent((T) event);
            }
        }

        @Override public void dispose() {
            synchronized (EventListenerRegistry.this) {
                if (isActive) {
                    markDisposed();
                    subscriptions.remove(this);
                }
            }
        }

        @Override public boolean isActive() {
            return isActive;
        }
    }

    public record LocalizeOnEventHook(
        OnEventHook hook,
        WeakReference<Localize> reference
    ) implements EventListener<LocalizeEvent> {
        LocalizeOnEventHook(OnEventHook hook, Localize localize) {
            this(hook, new WeakReference<>(localize));
        }

        @Override public void onEvent(LocalizeEvent event) {
            Localize localize = reference.get();

            if (localize != null) {
                hook.onEvent(localize, event);
            }
        }

        public boolean isGarbageCollected() {
            return reference.get() == null;
        }
    }

    @FunctionalInterface
    public interface OnEventHook {
        void onEvent(Localize localize, LocalizeEvent event);
    }
}
