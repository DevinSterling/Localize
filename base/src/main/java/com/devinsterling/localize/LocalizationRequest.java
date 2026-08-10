package com.devinsterling.localize;

import java.util.Map;
import java.util.Objects;

/// A Request to format an associated localized value with.
///
/// @since 1.0
public class LocalizationRequest {
    private final String key;
    private final String defaultValue;
    private final Arguments arguments;

    private LocalizationRequest(String key, String defaultValue, Arguments arguments) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
        this.defaultValue = defaultValue;
    }

    /// {@return The key associated with the requested value.}
    public String getKey() {
        return key;
    }

    /// {@return The default value associated with the requested value.}
    /// @since 1.1
    public String getDefaultValue() {
        return defaultValue;
    }

    /// Returns the arguments to format with.
    ///
    /// @return Arguments to format with.
    public Arguments getArguments() {
        return arguments;
    }

    /// {@return `true` if there is a non-null default value set.}
    /// @since 1.1
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    /// Checks if any arguments were provided.
    ///
    /// @return `true` if this request has arguments.
    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    /// Builder to build a [LocalizationRequest] for retrieval of a formatted localized value.
    ///
    /// @see Builder#of(String) to instantiate a builder instance.
    /// @since 1.1
    public static class Builder {
        private final String key;
        private String defaultValue;
        private Arguments arguments = Arguments.NONE;

        private Builder(String key) {
            this.key = key;
        }

        /// Sets the default value to return if the key is not found.
        ///
        /// @param defaultValue Default value.
        /// @return This builder instance.
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /// Sets the position or named arguments to format with.
        ///
        /// **Note**: Numbered arguments keys are numbers in string form, such as `"0"`, `"1"`, etc.
        ///
        /// @param arguments Positional or Named arguments.
        /// @return This builder instance.
        /// @throws NullPointerException If `arguments` is `null`.
        /// @since 2.0
        public Builder arguments(Arguments arguments) {
            this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
            return this;
        }

        /// Builds a [LocalizationRequest] instance.
        ///
        /// @return Request to get a formatted localized value with.
        /// @throws NullPointerException If `key` or `arguments` is `null`.
        public LocalizationRequest build() {
            return new LocalizationRequest(key, defaultValue, arguments);
        }

        /// Creates a builder instance to construct a [LocalizationRequest].
        ///
        /// @param key Key associated with the requested value.
        /// @return    Builder instance for a [LocalizationRequest].
        public static Builder of(String key) {
            return new Builder(key);
        }
    }
}
