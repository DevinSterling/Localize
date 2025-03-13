package com.devinsterling.localize;

import java.util.Map;
import java.util.Objects;

/// Request instance to format an associated localized value with.
///
/// @since 1.0
public class LocalizationRequest {
    private final String key;
    private final Map<String, Object> arguments;

    /// Instantiate a request to get a formatted localized value.
    ///
    /// @param key       Key associated with the requested value.
    /// @param arguments Positional or Named arguments to format with.
    public LocalizationRequest(String key, Map<String, Object> arguments) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");

        this.key = key;
        this.arguments = arguments;
    }

    /// {@return Key associated with the requested value.}
    public String getKey() {
        return key;
    }

    /// Named or numbered arguments to format with.
    ///
    /// Numbered arguments keys are numbers in string form.
    ///
    /// @return Immutable arguments map to format with.
    public Map<String, Object> getArguments() {
        return arguments;
    }

    /// Check if any named or numbered arguments were provided.
    ///
    /// @return True if this request has arguments.
    public boolean hasArguments() {
        return !arguments.isEmpty();
    }
}
