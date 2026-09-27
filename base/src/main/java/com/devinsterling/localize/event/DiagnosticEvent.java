package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;

/// An [event][LocalizeEvent] that provides diagnostic information.
///
/// Diagnostic events are informational and do not require handlers
/// to take action or alter the outcome of the operation that produced the
/// event.
///
/// ### Events
/// - [MissingKey]
/// - [ListenerExceptionCaught]
/// - [FormatterEvent.ExceptionCaught]
/// - [ProviderEvent.ExceptionCaught]
///
/// @since 2.0
public interface DiagnosticEvent extends LocalizeEvent {
    /// An event indicating that no corresponding resource value was found for a given [LocalizationRequestSource.Key].
    ///
    /// @see Localize#get(String)
    interface MissingKey extends DiagnosticEvent {
        /// Returns the key that has no corresponding resource value.
        ///
        /// @return Missing key.
        LocalizationRequestSource.Key getKey();

        /// Returns the request the missing key originates from.
        ///
        /// @return Request associated with the missing key.
        LocalizationRequest getRequest();
    }

    /// An event indicating that an unexpected [Exception] was
    /// [caught][getException] while calling [EventListener#onEvent].
    ///
    /// This event is propagated regardless of whether
    /// [LocalizeConfig#isIgnoreListenerExceptions()] is toggled or not.
    interface ListenerExceptionCaught extends DiagnosticEvent {
        /// Returns the listener where the [unexpected exception][getException] occurred from.
        ///
        /// @return Source listener.
        EventListener<?> getListener();

        /// Returns the event passed to [EventListener#onEvent].
        ///
        /// @return Source event.
        LocalizeEvent getEvent();

        /// Returns the caught exception.
        ///
        /// @return Caught exception.
        Exception getException();
    }
}
