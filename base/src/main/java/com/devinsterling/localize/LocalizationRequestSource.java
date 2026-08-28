package com.devinsterling.localize;

import java.util.Objects;

/// A source to derive a formatted localized value from, specifically a
/// [`Key`][LocalizationRequestSource.Key] or [`Pattern`][LocalizationRequestSource.Pattern].
///
/// ### Note
/// The source can be handled using exhaustive pattern matching:
/// ```
/// switch (source) {
///     case Key(String key) -> { ... }
///     case Pattern(String pattern) -> { ... }
/// }
/// ```
/// @since 2.0
public sealed interface LocalizationRequestSource permits LocalizationRequestSource.Key, LocalizationRequestSource.Pattern {

    /// A resource key used to perform lookup from a [`ResourceBundle`][java.util.ResourceBundle].
    ///
    /// @param value Resource key (e.g., `MyApp.welcome`).
    record Key(String value) implements LocalizationRequestSource {
        /// Creates a request source key.
        ///
        /// @param value Resource key.
        /// @throws NullPointerException If `value` is `null`.
        public Key(String value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }
    }

    /// A pattern to format directly.
    ///
    /// @param value Pattern (e.g., `Hi {name}!`).
    record Pattern(String value) implements LocalizationRequestSource {
        /// Creates a request source pattern.
        ///
        /// @param value Pattern.
        /// @throws NullPointerException If `value` is `null`.
        public Pattern(String value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }
    }
}
