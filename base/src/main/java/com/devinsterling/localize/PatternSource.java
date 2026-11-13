package com.devinsterling.localize;

import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/// Thread-safe key-value data source that returns patterns,
/// which are later formatted by a [LocalizationFormatter].
///
/// A pattern source is any data source that performs lookup using
/// string keys and returns an unformatted string message.
///
/// For example, a pattern source can be:
/// - A [ResourceBundle]:
///   ```java
///   PatternSource source = PatternSource.of(ResourceBundle.getBundle("messages", locale));
///   String welcome = source.getPattern("App.welcomeMessage");
///   String hi = source.getPattern("App.hi");
///
///   assert welcome.equals("Welcome {user}!");
///   assert hi.equals("Hello world!");
///   ```
/// - A switch statement:
///   ```java
///   PatternSource source = key -> switch (key) {
///       case "App.welcomeMessage" -> "Welcome {user}!";
///       case "App.hi" -> "Hello world!";
///       default -> null;
///   };
///   ```
/// - An immutable [`Map`][java.util.Map]:
///   ```java
///   Map<String, String> map = Map.of(
///       "App.welcomeMessage", "Welcome {user}!",
///       "App.hi", "Hello world!"
///   );
///   PatternSource source = map::get;
///   ```
///
/// @implSpec Implementations must be thread-safe for concurrent access.
/// @see of
/// @see PatternSourceProvider
/// @since 2.0
@FunctionalInterface
public interface PatternSource {
    /// Returns the pattern associated with the given key, if present.
    ///
    /// @param key Pattern lookup key.
    /// @return    Associated pattern, or `null` if not found.
    /// @throws NullPointerException If `key` is `null`.
    String getPattern(String key);

    /// Returns a [PatternSource] wrapper around the given [ResourceBundle].
    ///
    /// Values contained within the bundle that are not instances of
    /// [String] are treated as absent when calling [getPattern].
    ///
    /// @param bundle Resource bundle to wrap.
    /// @return       Wrapper around `bundle`.
    /// @throws NullPointerException If `bundle` is `null`.
    static PatternSource of(ResourceBundle bundle) {
        record ResourceBundleSource(ResourceBundle bundle) implements PatternSource {
            @Override public String getPattern(String key) {
                try {
                    // A mutable bundle may change while calling `containsKey` then `getObject`.
                    // Calling them sequentially would be error-prone as it's not an atomic operation.
                    return bundle.getObject(key) instanceof String s ? s : null;
                } catch (MissingResourceException e) {
                    return null;
                }
            }
        }

        return new ResourceBundleSource(Objects.requireNonNull(bundle, "bundle must not be null"));
    }
}
