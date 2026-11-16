package com.devinsterling.localize;

import java.util.Objects;

/// Signals that no value could be produced for a specified [LocalizationRequest].
///
/// @see LocalizeConfig#setThrowOnMissingValue
/// @since 2.0
public class MissingValueException extends RuntimeException {
    /// Localize instance that produced the missing value.
    private final Localize localize;
    /// Request associated with the missing value.
    private final LocalizationRequest request;

    /// Creates a [MissingValueException].
    ///
    /// @param message Exception detail message.
    /// @param localize Localize instance that produced the missing value.
    /// @param request Request associated with the missing value.
    /// @throws NullPointerException If `message`, `localize`, or `request` is `null`.
    public MissingValueException(String message, Localize localize, LocalizationRequest request) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.localize = Objects.requireNonNull(localize, "localize must not be null");
        this.request = Objects.requireNonNull(request, "request must not be null");
    }

    /// Creates a [MissingValueException].
    ///
    /// @param message  Exception detail message.
    /// @param localize Localize instance that produced the missing value.
    /// @param request  Request associated with the missing value.
    /// @param cause    Cause of the exception.
    ///                 (A `null` value is permitted, and indicates that the cause is nonexistent or unknown.)
    /// @throws NullPointerException If `message`, `localize`, or `request` is `null`.
    public MissingValueException(String message, Localize localize, LocalizationRequest request, Throwable cause) {
        this(message, localize, request);
        initCause(cause);
    }

    /// Returns the Localize instance that produced the missing value.
    ///
    /// @return Localize instance that produced the missing value.
    public Localize getLocalize() {
        return localize;
    }

    /// Returns the request associated with the missing value.
    ///
    /// @return Request associated with the missing value.
    public LocalizationRequest getRequest() {
        return request;
    }
}
