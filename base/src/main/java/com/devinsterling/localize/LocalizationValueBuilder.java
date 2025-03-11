package com.devinsterling.localize;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// Builder instance to retrieve formatted localized string values.
///
/// **Builder instances are not thread-safe.**
///
/// Support for both named and numbered arguments is included.
/// Note that mixing such calls will throw an [IllegalStateException].
///
/// ### Example
/// ```
/// // GOOD
/// builder.arg("test") // Argument 0
///        .arg("value2") // Argument 1
///        .arg("value3") // Argument 2
///        .value();
///
/// // BAD (This will throw an exception)
/// builder.arg("test")
///        .arg("key", "value")
///        .arg("value3")
///        .value();
/// ```
///
/// Arguments may be appended in bulk consecutively:
/// ```
/// // Numbered arguments
/// builder.args("test", "value2", "value3")
///        .args("value4", "value5")
///        .value();
///
/// // Named arguments
/// builder.args(Map.of("key1", "value1", "key2", "value2"))
///        .args(Map.of("key3", "value3"))
///        .value();
/// ```
/// @param <B> Builder instance type.
/// @since 1.0
public class LocalizationValueBuilder<B extends LocalizationValueBuilder<B>> {
    private final Applier applier;
    private final String key;
    private final Map<String, Object> values = new HashMap<>();
    private boolean isNamedArgs;
    private boolean isNumberedArgs;

    /// Instantiate a builder to request a specified localized value.
    ///
    /// @param key     Key to request a formatted localized value for.
    /// @param applier Callback to apply the properties of this builder
    ///                to the requested value.
    /// @throws NullPointerException if `key` or `applier` is `null`.
    public LocalizationValueBuilder(String key, Applier applier) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(applier, "Applier must not be null");
        this.key = key;
        this.applier = applier;
    }

    /// Append named argument key-value pairings.
    ///
    /// @param args Named argument key-value pairings.
    /// @return     this builder instance
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given map or keys contained are `null`.
    public B args(Map<String, Object> args) {
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            arg(entry.getKey(), entry.getValue());
        }
        return getBuilder();
    }

    /// Append an array of numbered argument values.
    ///
    /// **Calling this method alongside methods that accept
    /// named arguments such as [#arg(String, Object)]
    /// can cause undefined behavior.**
    ///
    /// @param args Numbered argument values
    /// @return     this builder instance
    /// @throws IllegalStateException If named arguments were added prior.
    /// @throws NullPointerException If the given array is `null`.
    public B args(Object... args) {
        checkIsNumberedArgs();
        for (Object arg : args) {
            values.put(String.valueOf(values.size()), arg);
        }
        return getBuilder();
    }

    /// Add a numbered argument with an associated value.
    ///
    /// **Calling this method alongside methods that accept
    /// named arguments such as [#arg(String, Object)]
    /// can cause undefined behavior.**
    ///
    /// @param value Numbered argument value
    /// @return      this builder instance
    /// @throws IllegalStateException If named arguments were added prior.
    /// @see #args(Object...)
    /// @see #arg(String, Object)
    public B arg(Object value) {
        checkIsNumberedArgs();
        values.put(String.valueOf(values.size()), value);
        return getBuilder();
    }

    /// Add a named argument with an associated value.
    ///
    /// @param key   Named argument key
    /// @param value Named argument value
    /// @return      this builder instance
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given key is `null`.
    /// @see #args(Map)
    /// @see #arg(Object)
    public B arg(String key, Object value) {
        Objects.requireNonNull(key, "Argument key must not be null");
        checkIsNamedArgs();
        values.put(key, value);
        return getBuilder();
    }

    /// Retrieve a formatted string with all properties
    /// applied from this builder.
    ///
    /// @return The formatted localized value.
    public String value() {
        return applier.evaluate(new LocalizationRequest(key, Map.copyOf(values)));
    }

    /// {@return The underlying applier}
    protected Applier getApplier() {
        return applier;
    }

    /// {@return The underlying key}
    protected String getKey() {
        return key;
    }

    /// {@return The underlying argument map}
    protected Map<String, Object> getArguments() {
        return values;
    }

    /// {@return This builder instance}
    @SuppressWarnings("unchecked")
    protected B getBuilder() {
        return (B) this;
    }

    private void checkIsNumberedArgs() {
        if (isNamedArgs) {
            throw new IllegalStateException("");
        }
        isNumberedArgs = true;
    }

    private void checkIsNamedArgs() {
        if (isNumberedArgs) {
            throw new IllegalStateException("");
        }
        isNamedArgs = true;
    }

    /// Callback to fetch the value of a requested string
    /// from the properties provided to a [LocalizationValueBuilder] instance.
    @FunctionalInterface
    public interface Applier {
        /// Apply the requested properties provided into
        /// an appropriately formatted string.
        ///
        /// @param request Requested properties to apply.
        /// @return The formatted value.
        String evaluate(LocalizationRequest request);
    }
}
