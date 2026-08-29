package com.devinsterling.localize;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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
    private final ArgumentsHelper arguments;
    private final LocalizationRequestSource source;
    private final Localize localize;
    private String defaultValue;

    /// Creates a builder to request a specified localized value.
    ///
    /// @param source   Source to derive a formatted localized value from.
    /// @param localize Localization instance to handle requests.
    /// @throws NullPointerException if `source` or `localize` is `null`.
    protected LocalizationValueBuilder(LocalizationRequestSource source, Localize localize) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.localize = Objects.requireNonNull(localize, "localize must not be null");
        this.arguments = new ArgumentsHelper(localize.getProcessor().argumentsHint());
    }

    /// Appends named argument key-value pairings.
    ///
    /// @param args Named argument key-value pairings.
    /// @return     This builder instance.
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given map or keys contained are `null`.
    public B args(Map<String, Object> args) {
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "Argument key must not be null");
            arguments.add(key, interceptValue(entry.getValue()));
        }
        return getBuilder();
    }

    /// Appends an array of numbered argument values.
    ///
    /// @param args Numbered argument values.
    /// @return     This builder instance.
    /// @throws IllegalStateException If named arguments were added prior.
    /// @throws NullPointerException If the given array is `null`.
    public B args(Object... args) {
        for (Object arg : args) {
            arguments.add(interceptValue(arg));
        }
        return getBuilder();
    }

    /// Adds a numbered argument with an associated value.
    ///
    /// @param value Numbered argument value.
    /// @return      This builder instance.
    /// @throws IllegalStateException If named arguments were added prior.
    /// @see #args(Object...)
    /// @see #arg(String, Object)
    public B arg(Object value) {
        arguments.add(interceptValue(value));
        return getBuilder();
    }

    /// Adds a numbered *deferred* argument that is supplied during formatting.
    ///
    /// @param valueSupplier Numbered argument value.
    /// @return              This builder instance.
    /// @throws IllegalStateException If named arguments were added prior.
    public B arg(Supplier<?> valueSupplier) {
        return arg((Object) valueSupplier);
    }

    /// Adds a named argument with an associated value.
    ///
    /// @param key   Named argument key.
    /// @param value Named argument value.
    /// @return      This builder instance.
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given key is `null`.
    /// @see #args(Map)
    /// @see #arg(Object)
    public B arg(String key, Object value) {
        Objects.requireNonNull(key, "Argument key must not be null");
        arguments.add(key, interceptValue(value));
        return getBuilder();
    }

    /// Adds a named *deferred* argument that is supplied during formatting.
    ///
    /// @param key           Named argument key.
    /// @param valueSupplier Named argument value.
    /// @return              This builder instance.
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given key is `null`.
    public B arg(String key, Supplier<?> valueSupplier) {
        return arg(key, (Object) valueSupplier);
    }

    /// Sets the default value if the requested key does not exist.
    ///
    /// @param defaultValue Default value.
    /// @return             This builder instance.
    /// @see                LocalizeConfig#setDefaultMissingValue(String)
    /// @since 1.1
    public B defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return getBuilder();
    }

    /// Retrieves a formatted string with all properties applied from this builder.
    ///
    /// @return The formatted localized value.
    public String value() {
        return localize.applyBuilderProperties(
            LocalizationRequest.Builder
                .of(getSource())
                .defaultValue(getDefaultValue())
                .arguments(arguments.get().resolve(getResolver()))
                .build()
        );
    }

    /// Intercepts an argument value before it is stored.
    ///
    /// This method allows subclasses to transform argument values.
    /// For example, replacing the original value with a deferred or computed value.
    ///
    /// ### Note
    /// When overriding, subclasses may call `super.interceptValue` to preserve default behavior.
    /// ```
    /// @Override protected Object interceptValue(Object value) {
    ///     if (value instanceof TextField field) {
    ///         registerListener(field, TextField::addListener, TextField::removeListener);
    ///         value = (Supplier<String>) field::getText;
    ///     }
    ///
    ///     // Calling super can be performed before or after main logic
    ///     return super.interceptValue(key, value);
    /// }
    /// ```
    ///
    /// @param value Argument value to potentially transform.
    /// @return      The transformed or original value.
    protected Object interceptValue(Object value) {
        return value;
    }

    /// Returns a snapshot of the current arguments.
    ///
    /// After this method call, arguments added through this builder are not included in the returned snapshot.
    ///
    /// @return Arguments snapshot.
    protected final Arguments snapshotArguments() {
        return arguments.snapshot();
    }

    protected Arguments.Resolver getResolver() {
        return DefaultArgumentsResolver.INSTANCE;
    }

    /// Returns the localization instance to handle requests.
    ///
    /// @return Localization instance.
    /// @since 2.0
    protected Localize getLocalize() {
        return localize;
    }

    /// Returns the source to derive a formatted localized value from.
    ///
    /// @return Request source.
    /// @since 2.0
    protected LocalizationRequestSource getSource() {
        return source;
    }

    /// {@return The underlying default value}
    /// @since 1.1
    protected String getDefaultValue() {
        return defaultValue;
    }

    /// {@return This builder instance}
    @SuppressWarnings("unchecked")
    protected B getBuilder() {
        return (B) this;
    }
}
