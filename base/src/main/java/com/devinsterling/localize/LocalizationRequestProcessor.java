package com.devinsterling.localize;

import java.util.ResourceBundle;

/// Processes a request to provide a formatted localized string.
///
/// @since 1.0
@FunctionalInterface
public interface LocalizationRequestProcessor {
    /// Process a [LocalizationRequest] with a specified [ResourceBundle]
    /// to retrieve a formatted localized string.
    ///
    /// @param bundle  Bundle to perform a lookup on.
    /// @param request Request to get and format the value by.
    /// @return Formatted localized string or `null` if not found.
    String process(ResourceBundle bundle, LocalizationRequest request);
}
