package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationFormatter;

import com.ibm.icu.message2.MessageFormatter;
import com.ibm.icu.text.MessageFormat;

import java.util.Map;
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
        String value = request.getPattern();
        Map<String, Object> arguments = request.getArguments().toNamedMap();

        value = switch (getConfig().getMessageFormat().getType()) {
            case V1_MESSAGE_FORMAT -> new MessageFormat(value, request.getLocale())
                    .format(arguments);
            case V2_MESSAGE_FORMAT -> MessageFormatter.builder()
                    .setLocale(request.getLocale())
                    .setPattern(value)
                    .build()
                    .formatToString(arguments);
        };

        return value;
    }

    /// Returns the formatter configuration.
    ///
    /// @return Formatter configuration.
    public IcuFormatterConfig getConfig() {
        return config;
    }
}
