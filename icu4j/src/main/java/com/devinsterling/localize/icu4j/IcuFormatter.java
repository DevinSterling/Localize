package com.devinsterling.localize.icu4j;

/// The ICU formatter of an [IcuProcessor].
///
/// @since 2.0
public final class IcuFormatter {
    private final static String V1_PATH = "com.ibm.icu.text.MessageFormat";
    private final static String V2_PATH = "com.ibm.icu.message2.MessageFormatter";

    /// Legacy ICU message formatter ([com.ibm.icu.text.MessageFormat])
    ///
    /// [MESSAGE2_FORMATTER] is the successor of this formatter.
    public static final IcuFormatter MESSAGE1_FORMATTER = new IcuFormatter(FormatterType.V1_MESSAGE_FORMAT);

    /// ICU message formatter ([com.ibm.icu.message2.MessageFormatter])
    public static final IcuFormatter MESSAGE2_FORMATTER = new IcuFormatter(FormatterType.V2_MESSAGE_FORMATTER);

    private final FormatterType type;

    private IcuFormatter(FormatterType type) {
        this.type = type;
    }

    static IcuFormatter from(String format) {
        return switch (format) {
            case V1_PATH -> MESSAGE1_FORMATTER;
            case V2_PATH -> MESSAGE2_FORMATTER;
            default -> throw new IllegalStateException(
                "Formatter type must be '" + V1_PATH + "' or '" + V2_PATH + "', got: " + format
            );
        };
    }

    FormatterType getType() {
        return type;
    }

    // An enum is not publicly exposed because they are not forward-compatible.
    // Using an enum here simplifies pattern matching.
    enum FormatterType {
        V1_MESSAGE_FORMAT,
        V2_MESSAGE_FORMATTER,
    }
}
