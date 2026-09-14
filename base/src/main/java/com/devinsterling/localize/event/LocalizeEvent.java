package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

/// An event propagated from a [`Localize`][com.devinsterling.localize.Localize] instance.
///
/// **Events are value-based**.
/// Programmers should treat instances that are equal as interchangeable and never use instances for synchronization,
/// or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
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
    /// @see Cause#EXTERNAL
    /// @see Cause#LOCALE_CHANGE
    default Cause getCause() {
        return Cause.EXTERNAL;
    }

    /// The cause of a [LocalizeEvent], identifying what triggered it.
    interface Cause {
        /// A cause triggered by an external method call.
        Cause EXTERNAL = createCause("EXTERNAL");

        /// A cause triggered by a locale change via [Localize#setLocale].
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
