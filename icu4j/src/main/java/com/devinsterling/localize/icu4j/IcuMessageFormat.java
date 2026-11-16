package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.LocalizationFormatter;

import com.ibm.icu.message2.MessageFormatter;
import com.ibm.icu.text.MessageFormat;

/// The ICU message format of an [IcuFormatter].
///
/// @since 2.0
public final class IcuMessageFormat {
    private final static String V1_PATH = "com.ibm.icu.text.MessageFormat";
    private final static String V2_PATH = "com.ibm.icu.message2.MessageFormatter";

    /// ICU Message Format ([com.ibm.icu.text.MessageFormat]).
    ///
    /// [MESSAGE2_FORMAT] is the successor of this format.
    public static final IcuMessageFormat MESSAGE1_FORMAT = new IcuMessageFormat(MF1::format);

    /// ICU Message Format 2 ([com.ibm.icu.message2.MessageFormatter]).
    ///
    /// ### Note
    /// Message Format 2 (MF2) is **non-final** and has undergone several
    /// syntax changes across releases (e.g., ICU4J 72.1-79.1).
    ///
    /// The minimum compatible version for MF2 is ICU4J 72.1 since 2022.
    // A lambda is used instead of a method reference to defer linkage to the optional MF2 implementation
    // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/invoke/LambdaMetafactory.html#:~:text=For%20method%20references
    @SuppressWarnings("Convert2MethodRef")
    public static final IcuMessageFormat MESSAGE2_FORMAT = new IcuMessageFormat(request -> MF2.format(request));

    private final LocalizationFormatter strategy;

    private IcuMessageFormat(LocalizationFormatter strategy) {
        this.strategy = strategy;
    }

    static IcuMessageFormat from(String format) {
        return switch (format) {
            case V1_PATH -> MESSAGE1_FORMAT;
            case V2_PATH -> MESSAGE2_FORMAT;
            default -> throw new IllegalStateException(
                "Message format type must be '" + V1_PATH + "' or '" + V2_PATH + "', got: " + format
            );
        };
    }

    LocalizationFormatter getStrategy() {
        return strategy;
    }

    private static final class MF1 {
        private static String format(LocalizationFormatter.Request request) {
            // Return early; nothing for `MessageFormat` to do as there are no arguments or quotes to unescape.
            if (request.getArguments().isEmpty() && request.getPattern().indexOf('\'') < 0) {
                return request.getPattern();
            }

            MessageFormat formatter = new MessageFormat(request.getPattern(), request.getLocale());

            // Prefer named arguments over positional (see IcuFormatter#argumentsHint)
            return formatter.format(request.getArguments().toNamedMap());
        }
    }

    private static final class MF2 {
        private static String format(LocalizationFormatter.Request request) {
            MessageFormatter formatter = MessageFormatter.builder()
                    .setLocale(request.getLocale())
                    .setPattern(request.getPattern())
                    .build();

            return formatter.formatToString(request.getArguments().toNamedMap());
        }
    }
}
