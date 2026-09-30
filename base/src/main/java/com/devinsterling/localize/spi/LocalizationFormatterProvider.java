package com.devinsterling.localize.spi;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizationFormatter;

/// A provider of localization formatters.
///
/// Implementations may return the same singleton instance or create a new [LocalizationFormatter]
/// on every call to [provide].
///
/// ### Minimal Example ・ Creating a Provider
/// Create a custom formatter using SPI:
/// ```java
/// package com.example.app;
///
/// import com.devinsterling.localize.Arguments;
/// import com.devinsterling.localize.LocalizationFormatter;
/// import com.devinsterling.localize.spi.LocalizationFormatterProvider;
/// import java.util.Locale;
///
/// public final class MyFormatter implements LocalizationFormatter {
///     private final CustomArgumentsFormatter formatter = new CustomArgumentsFormatter();
///
///     @Override String format(Request request) {
///         String pattern = request.getPattern();
///         Locale locale = request.getLocale();
///         Arguments arguments = request.getArguments();
///
///         return formatter.format(pattern, locale, arguments.toNamedMap());
///     }
///
///     // Creating a provider for SPI
///     public final class MyProvider implements LocalizationFormatterProvider {
///
///         @Override MyFormatter provide() {
///             return new MyFormatter();
///         }
///     }
/// }
/// ```
/// Register the provider in `module-info.java`:
/// ```java
/// provides com.devinsterling.localize.spi.LocalizationFormatterProvider
///     with com.example.app.MyCustomFormatter.MyProvider;
/// ```
///
/// @since 2.0
public interface LocalizationFormatterProvider {
    /// Returns a formatter.
    ///
    /// @return Formatter to format requests.
    /// @implSpec This method must be thread-safe.
    LocalizationFormatter provide();

    /// Returns the priority of this provider.
    ///
    /// A higher priority takes precedence over a lower priority. For example, `1` has higher priority than `-5`.
    ///
    /// When multiple providers are available, the provider with the highest priority is selected to
    /// supply default [LocalizationFormatter] instances to all newly instantiated [Localize] instances.
    ///
    /// The default implementation returns `0`.
    ///
    /// @return Provider priority.
    default int getPriority() {
        return 0;
    }
}
