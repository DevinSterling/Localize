package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

import java.util.Objects;
import java.util.function.Consumer;

/// A thread-safe event listener to handle events from [Localize] instances.
///
/// To prevent memory leaks, listeners must be removed when no longer needed
/// using [Subscription#dispose] or [Localize#removeListener].
///
/// ### Example Usage
/// Listener lifecycle:
/// ```
/// Localize localize = Localize.of(Locale.ENGLISH);
/// // Creating a listener
/// EventListener<LocaleChangeEvent> listener = event -> {
///     logger.info("Locale changed to {}", event.getNewLocale());
/// };
///
/// // Registering a listener
/// Subscription subscription = localize.addListener(LocaleChangeEvent.class, listener);
///
/// // Deregistering a listener
/// subscription.dispose();
/// // or
/// localize.removeListener(LocaleChangeEvent.class, listener);
/// ```
///
/// ### Note
/// When subclassing [Localize], creating an [EventListener] is not needed.
/// Overridable hooks are provided there (e.g., `Localize#onEvent`).
///
/// @param <T> Event type.
/// @since 2.0
@FunctionalInterface
public interface EventListener<T extends LocalizeEvent> {
    /// A handler triggered whenever an event occurs.
    ///
    /// @param event Occurred event.
    /// @implSpec This method must be thread-safe.
    void onEvent(T event);
}
