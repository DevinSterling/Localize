package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

/// An event propagated from a [`Localize`][com.devinsterling.localize.Localize] instance.
///
/// ### Events
/// - [DiagnosticEvent]
/// - [FormatterEvent]
/// - [LocaleEvent]
/// - [ProviderEvent]
///
/// **Events are value-based**.
/// Programmers should treat instances that are equal as interchangeable and never use instances for synchronization,
/// or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
///
/// ### Event Handling
/// Events are handled by passing an [EventListener] to [Localize#addListener].
///
/// @since 2.0
public interface LocalizeEvent {
    /// Returns the [Localize] instance the event originated from.
    ///
    /// The source identifies the instance the event was triggered from,
    /// unlike [getCause] which identifies what caused the event to be triggered.
    ///
    /// @return Source [Localize] instance.
    Localize getSource();

    /// Returns the cause, identifying what triggered the event.
    ///
    /// @return Event cause.
    /// @see Cause#DIRECT
    /// @see Cause#LOCALE_CHANGE
    default Cause getCause() {
        return Cause.DIRECT;
    }

    /// The cause of a [LocalizeEvent], identifying what triggered it.
    interface Cause {
        /// A cause indicating that the corresponding event is a direct result of an operation
        /// (e.g., [Localize#setFormatter], [Localize#clearProviders]).
        Cause DIRECT = createCause("DIRECT");

        /// A cause triggered as a side effect of a locale change via [Localize#setLocale].
        /// The locale change itself is already reported through [LocaleEvent].
        Cause LOCALE_CHANGE = createCause("LOCALE_CHANGE");

        private static Cause createCause(String cause) {
            record LocalizeCause(String cause) implements Cause {
                @Override public String toString() {
                    return cause;
                }
            }
            return new LocalizeCause(cause);
        }
    }
}
