package com.devinsterling.localize.icu4j;

/// The ICU message format of an [IcuFormatter].
///
/// @since 2.0
public final class IcuMessageFormat {
    private final static String V1_PATH = "com.ibm.icu.text.MessageFormat";
    private final static String V2_PATH = "com.ibm.icu.message2.MessageFormatter";

    /// Legacy ICU message format ([com.ibm.icu.text.MessageFormat]).
    ///
    /// [MESSAGE2_FORMAT] is the successor of this format.
    public static final IcuMessageFormat MESSAGE1_FORMAT = new IcuMessageFormat(IcuMessageFormatStrategy.FORMAT1);

    /// ICU message format 2 ([com.ibm.icu.message2.MessageFormatter]).
    public static final IcuMessageFormat MESSAGE2_FORMAT = new IcuMessageFormat(IcuMessageFormatStrategy.FORMAT2);

    private final IcuMessageFormatStrategy strategy;

    private IcuMessageFormat(IcuMessageFormatStrategy strategy) {
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

    IcuMessageFormatStrategy getStrategy() {
        return strategy;
    }
}
