package com.devinsterling.localize.spi;

import com.devinsterling.localize.LocalizationFormatter;

/// A provider of localization formatters.
///
/// Implementations may return the same singleton instance or create a new [LocalizationFormatter]
/// on every call to [provide].
///
/// @since 2.0
public interface LocalizationFormatterProvider {
    /// Returns a formatter.
    ///
    /// @implSpec This method is thread-safe.
    /// @return Formatter to format requests.
    LocalizationFormatter provide();
}
