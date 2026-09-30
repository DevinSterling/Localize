package com.devinsterling.localize;

/// Handler to provide a default value when no value is found for a specified key.
///
/// @see LocalizeConfig#setMissingValueHandler
/// @see LocalizationValueBuilder#defaultHandler
/// @since 2.0
@FunctionalInterface
public interface MissingValueHandler {
    /// Returns a resolved default value for the given request.
    ///
    /// @param source  Source instance.
    /// @param request Corresponding localization request.
    /// @param key     Key with no associated value.
    /// @return Resolved default value.
    String handle(Localize source, LocalizationRequest request, String key);

    /// Creates and returns a handler that always returns the given value.
    ///
    /// @param value Value to always return.
    /// @return Handler that always returns the given value.
    static MissingValueHandler of(String value) {
        record ConstantValue(String value) implements MissingValueHandler {
            @Override public String handle(Localize source, LocalizationRequest request, String key) {
                return value;
            }
        }
        return new ConstantValue(value);
    }
}
