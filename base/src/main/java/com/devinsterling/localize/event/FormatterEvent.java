package com.devinsterling.localize.event;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

/// An [event][LocalizeEvent] associated with the [LocalizationFormatter] of a [Localize] instance.
///
/// ### Events
/// - [Replaced]
/// - [ExceptionCaught]
///
/// @since 2.0
public interface FormatterEvent extends LocalizeEvent {
    /// An event indicating that the [LocalizationFormatter] was replaced.
    ///
    /// @see Localize#setFormatter
    interface Replaced extends FormatterEvent {
        /// Returns the old formatter.
        ///
        /// @return Old formatter.
        LocalizationFormatter getOldFormatter();

        /// Returns the new formatter.
        ///
        /// @return New formatter; the replacement.
        LocalizationFormatter getNewFormatter();
    }

    /// An event indicating that an unexpected [Exception] was
    /// [caught][getException] while calling [LocalizationFormatter#format].
    ///
    /// This event is propagated regardless of whether
    /// [LocalizeConfig#isIgnoreFormatterExceptions()] is toggled or not.
    interface ExceptionCaught extends FormatterEvent, DiagnosticEvent {
        /// Returns the formatter where the [unexpected exception][getException] occurred from.
        ///
        /// @return Source formatter.
        LocalizationFormatter getFormatter();

        /// Returns the request passed to [LocalizationFormatter#format].
        ///
        /// @return Source request.
        LocalizationFormatter.Request getRequest();

        /// Returns the caught exception.
        ///
        /// @return Caught exception.
        Exception getException();
    }
}
