package com.devinsterling.localize;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/// Provider that produces a [ResourceBundle] on demand with a given [Locale].
///
/// @see Localize#putBundleProvider(String, ResourceBundleProvider)
/// @since 1.0
@FunctionalInterface
public interface ResourceBundleProvider {
    /// Retrieves the [ResourceBundle] associated with the given [Locale].
    ///
    /// @param locale Locale for the produced resource bundle to be based upon.
    /// @return       The produced resource bundle.
    ResourceBundle getBundle(Locale locale);

    /// A unique key associated with a [ResourceBundleProvider].
    ///
    /// Keys are created using the static factory methods listed here:
    /// - [of()] to create a unique key.
    /// - [of(String)] to create a string key.
    ///
    /// @implSpec Implementations must ensure [Object#equals] and [Object#hashCode] are properly overridden, if needed.
    /// @since 2.0
    interface Key {
        /// Returns a new unique key that is only equal to itself.
        ///
        /// ### Example Usage
        /// Comparing equality:
        /// ```
        /// ResourceBundleProvider.Key a = ResourceBundleProvider.Key.of();
        /// ResourceBundleProvider.Key b = ResourceBundleProvider.Key.of();
        ///
        /// assert a.equals(b); // false
        /// ```
        /// @return New unique key.
        /// @see of(String)
        static Key of() {
            return new Key() {};
        }

        /// Returns a wrapper over a string key.
        ///
        /// ### Example Usage
        /// Comparing equality:
        /// ```
        /// ResourceBundleProvider.Key a = ResourceBundleProvider.Key.of("foo");
        /// ResourceBundleProvider.Key b = ResourceBundleProvider.Key.of("foo");
        /// ResourceBundleProvider.Key c = ResourceBundleProvider.Key.of("bar");
        ///
        /// assert a.equals(b); // true
        /// assert b.equals(c); // false
        /// ```
        /// @param key String key to wrap.
        /// @return    Wrapped string key.
        /// @throws NullPointerException If `key` is `null`.
        /// @see of()
        static Key of(String key) {
            record NamedKey(String key) implements Key {
                @Override public String toString() {
                    return key;
                }
            }

            return new NamedKey(Objects.requireNonNull(key, "key must not be null"));
        }
    }
}
