package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestProcessor;

import com.ibm.icu.message2.MessageFormatter;
import com.ibm.icu.text.MessageFormat;

import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/// Processes a request to provide an ICU formatted localized string.
///
/// @since 2.0
public class IcuProcessor implements LocalizationRequestProcessor {
    private final IcuProcessorConfig config;

    /// Creates an [IcuProcessor] instance with default configuration.
    public IcuProcessor() {
        this(new IcuProcessorConfig());
    }

    /// Creates an [IcuProcessor] instance with the desired configuration.
    ///
    /// @param config The configuration.
    /// @throws NullPointerException If `config` is `null`.
    public IcuProcessor(IcuProcessorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override public Arguments.Type argumentsHint() {
        return Arguments.Type.NAMED;
    }

    @Override public String process(LocalizationRequestProcessor.Context context) {
        ResourceBundle bundle = context.getBundle();
        LocalizationRequest request = context.getRequest();
        String value = null;

        if (bundle.containsKey(request.getKey())) {
            value = bundle.getString(request.getKey());

            if (request.hasArguments()) {
                Map<String, Object> arguments = request.getArguments().toNamedMap();

                value = switch (config.getFormatter().getType()) {
                    case V1_MESSAGE_FORMAT -> new MessageFormat(value, context.getLocale())
                            .format(arguments);
                    case V2_MESSAGE_FORMATTER -> MessageFormatter.builder()
                            .setLocale(context.getLocale())
                            .setPattern(value)
                            .build()
                            .formatToString(arguments);
                };
            }
        }

        return value;
    }

    /// Returns the processor configuration.
    ///
    /// @return Processor configuration.
    public IcuProcessorConfig getConfig() {
        return config;
    }
}
