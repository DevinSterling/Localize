package com.devinsterling.localize.event;

import java.util.Locale;

/// An event representing a [Locale] change in a [`Localize`][com.devinsterling.localize.Localize] instance.
///
/// In multithreaded applications, events may be propagated asynchronously across threads.
/// Handlers should check [isValid] to discard stale updates before processing [getNewLocale].
///
/// @see com.devinsterling.localize.Localize#setLocale
/// @since 2.0
public interface LocaleChangeEvent extends LocalizeEvent {
    /// Returns the locale that was set before this change.
    ///
    /// @return Old locale **(never `null`)**.
    Locale getOldLocale();

    /// Returns the new locale associated with this change.
    ///
    /// ### Note
    /// In multithreaded applications, events may be propagated asynchronously across threads.
    /// Newer calls to [`Localize#setLocale(Locale)`][com.devinsterling.localize.Localize#setLocale]
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
