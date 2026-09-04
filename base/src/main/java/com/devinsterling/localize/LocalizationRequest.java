package com.devinsterling.localize;

import java.util.Objects;

/// A request to format an associated localized value with.
///
/// **This is a value-based class**.
/// Programmers should treat instances that are equal as interchangeable,
/// avoid identity checks (`==`), and never use instances for synchronization,
/// or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
///
/// @since 1.0
public final class LocalizationRequest {
    private final LocalizationRequestSource source;
    private final String defaultValue;
    private final Arguments arguments;

    private LocalizationRequest(LocalizationRequestSource source, String defaultValue, Arguments arguments) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
        this.defaultValue = defaultValue;
    }

    /// Returns the source to derive a formatted localized value from.
    ///
    /// @return Request source.
    /// @since 2.0
    public LocalizationRequestSource getSource() {
        return source;
    }

    /// Returns the default value for the requested value, if any.
    ///
    /// @return Default value or `null` if not set.
    /// @see hasDefaultValue
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

    /// Checks if a default value is present.
    ///
    /// @return `true` if a non-null default value is set.
    /// @see getDefaultValue
    /// @since 1.1
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    // When value classes become stable, equals and hashcode will be removed here
    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LocalizationRequest other)) return false;
        return source.equals(other.source)
                && Objects.equals(defaultValue, other.defaultValue)
                && arguments.equals(other.arguments);
    }

    @Override public int hashCode() {
        return Objects.hash(source, defaultValue, arguments);
    }

    /// Builder to create a [LocalizationRequest] for retrieval of a formatted localized value.
    ///
    /// @see Builder#of(LocalizationRequestSource)
    /// @since 1.1
    public static final class Builder {
        private final LocalizationRequestSource source;
        private String defaultValue;
        private Arguments arguments = Arguments.NONE;

        private Builder(LocalizationRequestSource source) {
            this.source = source;
        }

        /// Creates a builder instance to construct a [LocalizationRequest].
        ///
        /// @param source Source to derive a formatted localized value from.
        /// @return       Builder instance for a [LocalizationRequest].
        /// @throws NullPointerException If `source` is `null`.
        public static Builder of(LocalizationRequestSource source) {
            return new Builder(Objects.requireNonNull(source, "source must not be null"));
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
        public LocalizationRequest build() {
            return new LocalizationRequest(source, defaultValue, arguments);
        }
    }
}
