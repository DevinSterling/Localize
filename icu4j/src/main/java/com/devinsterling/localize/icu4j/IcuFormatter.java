package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationFormatter;

import java.util.Objects;

/// Processes a request to provide an ICU formatted localized string.
///
/// @since 2.0
public class IcuFormatter implements LocalizationFormatter {
    private final IcuFormatterConfig config;

    /// Creates an [IcuFormatter] instance with default configuration.
    public IcuFormatter() {
        this(new IcuFormatterConfig());
    }

    /// Creates an [IcuFormatter] instance with the desired configuration.
    ///
    /// @param config The configuration.
    /// @throws NullPointerException If `config` is `null`.
    public IcuFormatter(IcuFormatterConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override public Arguments.Type argumentsHint() {
        return Arguments.Type.NAMED;
    }

    @Override public String format(Request request) {
        return getConfig().getMessageFormat().getStrategy().format(request);
    }

    /// Returns the formatter configuration.
    ///
    /// @return Formatter configuration.
    public IcuFormatterConfig getConfig() {
        return config;
    }
}
