package com.devinsterling.localize;

import java.util.Map;
import java.util.Objects;

/// A Request to format an associated localized value with.
///
/// @since 1.0
public class LocalizationRequest {
    private final String key;
    private final String defaultValue;
    private final Map<String, Object> arguments;

    /// Creates a request to get a formatted localized value.
    ///
    /// @deprecated      Prefer [Builder#of(String)] instead.
    /// @param key       Key associated with the requested value.
    /// @param arguments Positional or Named arguments to format with.
    /// @throws NullPointerException If `key` or `arguments` is `null`.
    @Deprecated(since = "1.1")
    public LocalizationRequest(String key, Map<String, Object> arguments) {
        this(key, null, arguments);
    }

    private LocalizationRequest(String key, String defaultValue, Map<String, Object> arguments) {
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

    /// Named or numbered arguments to format with.
    ///
    /// **Note**: Numbered arguments keys are numbers in string form, such as `"0"`, `"1"`, etc.
    ///
    /// @return Immutable arguments map to format with.
    public Map<String, Object> getArguments() {
        return arguments;
    }

    /// {@return `true` if there is a non-null default value set.}
    /// @since 1.1
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    /// Check if any named or numbered arguments were provided.
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
        private Map<String, Object> arguments = Map.of();

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
        public Builder arguments(Map<String, Object> arguments) {
            this.arguments = arguments;
            return this;
        }

        /// Builds a [LocalizationRequest] instance.
        ///
        /// @return Request to get a formatted localized value with.
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
