package com.devinsterling.localize;

/// Handler to provide a default value when no value is found for a specified [LocalizationRequest].
///
/// @see DEFAULT
/// @see LocalizeConfig#setMissingValueHandler
/// @see LocalizationValueBuilder#defaultHandler
/// @since 2.0
@FunctionalInterface
public interface MissingValueHandler {
    /// Returns a resolved default value for the given request.
    ///
    /// @param localize Source Localize instance.
    /// @param request  Corresponding localization request.
    /// @return Resolved default value.
    String handle(Localize localize, LocalizationRequest request);

    /// Creates and returns a handler that always returns the given value.
    ///
    /// @param value Value to always return.
    /// @return Handler that always returns the given value.
    static MissingValueHandler of(String value) {
        record ConstantValue(String value) implements MissingValueHandler {
            @Override public String handle(Localize source, LocalizationRequest request) {
                return value;
            }
        }
        return new ConstantValue(value);
    }

    /// The default [MissingValueHandler].
    ///
    /// The returned value depends on [LocalizationRequest#getSource()]:
    /// - When it is [LocalizationRequestSource.Key], the key is wrapped within square brackets.
    ///   > For example, `my-key` becomes `[my-key]` as the output.
    /// - When it is [LocalizationRequestSource.Pattern], the pattern is returned as-is.
    ///   > For example, `Hi {user}!` remains `Hi {user}!` as the output.
    MissingValueHandler DEFAULT = (localize, request) -> {
        String handle = "";

        // In Java 21, this will be replaced with a switch
        if (request.getSource() instanceof LocalizationRequestSource.Key key) {
            handle = "[" + key.value() + "]";
        } else if (request.getSource() instanceof LocalizationRequestSource.Pattern pattern) {
            handle = pattern.value();
        }

        return handle;
    };
}
