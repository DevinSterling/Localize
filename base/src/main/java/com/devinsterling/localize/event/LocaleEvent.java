package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

import java.util.Locale;

/// An [event][LocalizeEvent] associated with the [Locale] of a [Localize] instance.
///
/// ### Events
/// - [Replaced]
///
/// @since 2.0
public interface LocaleEvent extends LocalizeEvent {
    /// An event indicating that the [Locale] was replaced.
    ///
    /// A [Locale] is replaced by calling [Localize#setLocale].
    /// The previous locale is retrievable via [getOldLocale], and the newly set one via [getNewLocale].
    ///
    /// In multithreaded applications, events may be propagated asynchronously across threads.
    /// Handlers should check [isValid] before processing [getNewLocale] to discard stale updates.
    ///
    /// @see Localize#setLocale
    interface Replaced extends LocaleEvent {
        /// Returns the locale that was set before this change.
        ///
        /// @return Old locale **(never `null`)**.
        Locale getOldLocale();

        /// Returns the new locale associated with this change.
        ///
        /// ### Note
        /// In multithreaded applications, events may be propagated asynchronously across threads.
        /// Newer calls to [Localize#setLocale]
        /// may supersede this event before it is handled.
        ///
        /// Callers should check [isValid] to discard stale updates.
        ///
        /// @return New locale **(never `null`)**.
        Locale getNewLocale();

        /// Returns `true` if this event has **not** been superseded by a newer locale change.
        ///
        /// If this method returns `false`, [getNewLocale] is stale.
        ///
        /// @return `true` if this event is still current, or `false` if superseded.
        boolean isValid();
    }
}
