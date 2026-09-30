package com.devinsterling.localize;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// Builder to retrieve formatted localized string values.
///
/// **Builder instances are not thread-safe.**
///
/// ### Positional and Named Arguments
/// Both positional and named arguments are supported.
/// However, both argument styles cannot be mixed within the same builder instance
/// and attempting to do so will throw an [IllegalStateException]:
/// ```java
/// // GOOD
/// builder.arg("test")   // Argument 0
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
/// Arguments may also be appended in bulk:
/// ```java
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
///
/// ### Deferred Arguments
/// [LocalizationValueBuilder] supports deferred values as arguments by accepting suppliers:
/// ```java
/// builder.arg(weather::getTemperature);
/// ```
/// Supplier are invoked each time the formatted localized value is computed (e.g., calling [value]).
///
/// For memory-sensitive applications that want to avoid retaining the source object by strong reference,
/// Localize also supports weakly referencing the source separately from the method reference:
/// - [arg(Object, Function)] (Numbered argument variant)
///   ```java
///   builder.arg(weather, Weather::getTemperature);
///   ```
/// - [arg(String, Object, Function)] (Named argument variant)
///   ```java
///   builder.arg("humidity", weather, Weather::getHumidity);
///   ```
/// Note that if a source object is garbage collected, its deferred argument value is `null`.
///
/// @param <B> Builder instance type.
/// @since 1.0
public class LocalizationValueBuilder<B extends LocalizationValueBuilder<B>> {
    private final ArgumentsHelper arguments;
    private final LocalizationRequestSource source;
    private final Localize localize;
    private MissingValueHandler missingValueHandler;

    /// Creates a builder to request a specified localized value.
    ///
    /// @param source   Source to derive a formatted localized value from.
    /// @param localize Localization instance to handle requests.
    /// @throws NullPointerException if `source` or `localize` is `null`.
    protected LocalizationValueBuilder(LocalizationRequestSource source, Localize localize) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.localize = Objects.requireNonNull(localize, "localize must not be null");
        this.arguments = new ArgumentsHelper(localize.getFormatter().argumentsHint());
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
    /// @param valueSupplier Deferred value, invoked each time a formatted localized value is computed.
    /// @return              This builder instance.
    /// @throws IllegalStateException If named arguments were added prior.
    /// @since 2.0
    public B arg(Supplier<?> valueSupplier) {
        return arg((Object) valueSupplier);
    }

    /// Adds a numbered *deferred* argument backed by a weak reference to the given source.
    ///
    /// The source is weakly referenced so that it may be garbage collected when no longer in use.
    /// If the source has been garbage collected, the deferred argument value is `null`.
    ///
    /// @param <T>       Type of the source.
    /// @param <U>       Type of the deferred value.
    /// @param source    Source providing the value.
    /// @param getValue  Deferred value from the source,
    ///                  invoked each time a formatted localized value is computed.
    /// @return          This builder instance.
    /// @throws NullPointerException If `source` or `getValue` is `null`.
    /// @since 2.0
    public <T, U> B arg(T source, Function<T, U> getValue) {
        return arg(new WeakSupplier<>(source, getValue));
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
    /// @param valueSupplier Deferred value, invoked each time a formatted localized value is computed.
    /// @return              This builder instance.
    /// @throws IllegalStateException If numbered arguments were added prior.
    /// @throws NullPointerException If the given key is `null`.
    /// @since 2.0
    public B arg(String key, Supplier<?> valueSupplier) {
        return arg(key, (Object) valueSupplier);
    }

    /// Adds a named *deferred* argument backed by a weak reference to the given source.
    ///
    /// The source is weakly referenced so that it may be garbage collected when no longer in use.
    /// If the source has been garbage collected, the deferred argument value is `null`.
    ///
    /// @param <T>       Type of the source.
    /// @param <U>       Type of the deferred value.
    /// @param key       Key to be inserted.
    /// @param source    Source providing the value.
    /// @param getValue  Deferred value from the source,
    ///                  invoked each time a formatted localized value is computed.
    /// @return          This builder instance.
    /// @throws NullPointerException If `key`, `source`, or `getValue` is `null`.
    /// @since 2.0
    public <T, U> B arg(String key, T source, Function<T, U> getValue) {
        return arg(key, new WeakSupplier<>(source, getValue));
    }

    /// Sets the default value to use when no value is found for the specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// This is a convenience method, equivalent to calling:
    /// ```
    /// defaultValue(MissingValueHandler.of(defaultValue));
    /// ```
    ///
    /// @param defaultValue Default value.
    /// @return             This builder instance.
    /// @see                defaultHandler
    /// @see                LocalizeConfig#setMissingValueHandler
    /// @since 1.1
    public B defaultValue(String defaultValue) {
        return defaultHandler(MissingValueHandler.of(defaultValue));
    }

    /// Sets the handler to provide a default value when no value is found for the specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// The handler given here takes precedence over the handler set via [LocalizeConfig#setMissingValueHandler].
    ///
    /// @param handler Handler to provide a default value.
    /// @return        This builder instance.
    /// @since 2.0
    public B defaultHandler(MissingValueHandler handler) {
        missingValueHandler = handler;
        return getBuilder();
    }

    /// Retrieves a formatted string with all properties applied from this builder.
    ///
    /// @return Formatted localized value.
    public String value() {
        return localize.formatValue(
            LocalizationRequest.Builder
                .of(getSource())
                .missingValueHandler(getMissingValueHandler())
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
    /// @since 2.0
    protected Object interceptValue(Object value) {
        return value;
    }

    /// Returns an immutable snapshot of the current arguments.
    ///
    /// After this method call, arguments added through this builder are not included in the returned snapshot.
    ///
    /// @return Arguments snapshot.
    /// @since 2.0
    protected final Arguments snapshotArguments() {
        return arguments.snapshot();
    }

    /// Returns the argument resolver used before retrieving localized values.
    ///
    /// Default: [DefaultArgumentsResolver]
    /// @return Arguments resolver.
    /// @since 2.0
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

    /// Returns the handler to provide a default value when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// @return Handler or `null` if not set.
    /// @since 2.0
    protected MissingValueHandler getMissingValueHandler() {
        return missingValueHandler;
    }

    /// Returns this builder instance.
    ///
    /// @return This builder instance.
    @SuppressWarnings("unchecked")
    protected B getBuilder() {
        return (B) this;
    }

    private record WeakSupplier<T, U>(WeakReference<T> reference, Function<T, U> supplier) implements Supplier<U> {
        public WeakSupplier(T source, Function<T, U> getValue) {
            this(
                new WeakReference<>(Objects.requireNonNull(source, "source must not be null")),
                Objects.requireNonNull(getValue, "getValue must not be null")
            );
        }

        @Override public U get() {
            T value = reference.get();
            return value == null ? null : supplier.apply(value);
        }
    }
}
