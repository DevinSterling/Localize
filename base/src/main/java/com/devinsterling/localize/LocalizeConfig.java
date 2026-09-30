package com.devinsterling.localize;

import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.EventListener;
import com.devinsterling.localize.event.FormatterEvent;
import com.devinsterling.localize.event.ProviderEvent;

import java.util.MissingResourceException;
import java.util.Objects;

/// Configuration to control how [Localize] handles operations.
///
/// ### Default Configuration
/// - [isThrowOnMissingValue][setThrowOnMissingValue] = `false`
/// - [isIgnoreFormatterExceptions][setIgnoreFormatterExceptions] = `false`
/// - [isIgnoreProviderExceptions][setIgnoreProviderExceptions] = `false`
/// - [isIgnoreListenerExceptions][setIgnoreListenerExceptions] = `false`
/// - [missingValueHandler][LocalizeConfig#setMissingValueHandler]
///   = `(_, _, key) -> "[" + key + "]"` (e.g., `"[program.name]"`).
///
/// @since 1.0
public class LocalizeConfig {
    private static final MissingValueHandler DEFAULT_HANDLER = (s, r, key) -> "[" + key + "]";

    private volatile boolean isThrowOnMissingValue = false;
    private volatile boolean isIgnoreFormatterExceptions = false;
    private volatile boolean isIgnoreProviderExceptions = false;
    private volatile boolean isIgnoreListenerExceptions = false;
    private volatile MissingValueHandler missingValueHandler = DEFAULT_HANDLER;

    /// Creates a configuration instance with all values set to their defaults.
    public LocalizeConfig() {}

    /// Sets whether to throw a [MissingResourceException] when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// When set to `true`, a [MissingResourceException] is thrown when all the conditions are met:
    /// 1. **All** bundles contain no value for a specified key.
    /// 2. [LocalizationRequest#getMissingValueHandler] is `null` (Set by [LocalizationValueBuilder#defaultHandler]).
    ///
    /// Default: `false`
    /// @param isThrow `true` to throw an exception, or `false` to ignore.
    public void setThrowOnMissingValue(boolean isThrow) {
        this.isThrowOnMissingValue = isThrow;
    }

    /// Sets whether runtime exceptions from [localization formatters][LocalizationFormatter]
    /// are ignored while calling [LocalizationFormatter#format].
    ///
    /// When set to `true`, runtime exceptions are ignored.
    /// Otherwise, exceptions are thrown and propagated.
    ///
    /// Default: `false`
    /// @param ignore `true` to ignore exceptions, or `false` to throw.
    /// @see FormatterEvent.ExceptionCaught
    public void setIgnoreFormatterExceptions(boolean ignore) {
        this.isIgnoreFormatterExceptions = ignore;
    }

    /// Sets whether runtime exceptions from [resource bundle providers][ResourceBundleProvider]
    /// are ignored while calling [ResourceBundleProvider#getBundle].
    ///
    /// When set to `true`, runtime exceptions are ignored.
    /// Otherwise, exceptions are thrown and propagated.
    ///
    /// Default: `false`
    /// @param ignore `true` to ignore exceptions, or `false` to throw.
    /// @see ProviderEvent.ExceptionCaught
    public void setIgnoreProviderExceptions(boolean ignore) {
        this.isIgnoreProviderExceptions = ignore;
    }

    /// Sets whether runtime exceptions from [event listeners][EventListener]
    /// are ignored while calling [EventListener#onEvent].
    ///
    /// When set to `true`, runtime exceptions are ignored.
    /// Otherwise, exceptions are thrown and propagated.
    ///
    /// Default: `false`
    /// @param ignore `true` to ignore exceptions, or `false` to throw.
    /// @see DiagnosticEvent.ListenerExceptionCaught
    /// @since 2.0
    public void setIgnoreListenerExceptions(boolean ignore) {
        this.isIgnoreListenerExceptions = ignore;
    }

    /// Sets the handler to provide a default value when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// [LocalizationRequest#getMissingValueHandler] takes precedence over the handler given here.
    /// If it is `null`, then the handler set here is used when [isThrowOnMissingValue()] is disabled.
    ///
    /// Default: `(_, _, key) -> "[" + key + "]"` (e.g., `"[program.name]"`).
    /// @param handler Handler to provide a default value.
    /// @throws NullPointerException If `handler` is `null`.
    /// @since 2.0
    public void setMissingValueHandler(MissingValueHandler handler) {
        this.missingValueHandler = Objects.requireNonNull(handler, "handler must not be null");
    }

    /// Sets the default value to use when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// This is a convenience method, equivalent to calling:
    /// ```
    /// setMissingValueHandler(MissingValueHandler.of(defaultValue));
    /// ```
    ///
    /// @param defaultValue Default value.
    /// @see setMissingValueHandler
    public void setDefaultMissingValue(String defaultValue) {
        setMissingValueHandler(MissingValueHandler.of(defaultValue));
    }

    /// Returns `true` if a [MissingResourceException] is thrown when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// Default: `false`
    /// @return `true` if an exception is thrown, or `false` if ignored.
    public boolean isThrowOnMissingValue() {
        return isThrowOnMissingValue;
    }

    /// Returns `true` if runtime exceptions from [localization formatters][LocalizationFormatter]
    /// are ignored while calling [LocalizationFormatter#format].
    ///
    /// Default: `false`
    /// @return `true` if exceptions are ignored, or `false` if thrown.
    public boolean isIgnoreFormatterExceptions() {
        return isIgnoreFormatterExceptions;
    }

    /// Returns `true` if runtime exceptions from [resource bundle providers][ResourceBundleProvider]
    /// are ignored while calling [ResourceBundleProvider#getBundle].
    ///
    /// Default: `false`
    /// @return `true` if exceptions are ignored, or `false` if thrown.
    public boolean isIgnoreProviderExceptions() {
        return isIgnoreProviderExceptions;
    }

    /// Returns `true` if runtime exceptions from [event listeners][com.devinsterling.localize.event.EventListener]
    /// are ignored while calling [EventListener#onEvent].
    ///
    /// Default: `false`
    /// @return `true` if exceptions are ignored, or `false` if thrown.
    /// @since 2.0
    public boolean isIgnoreListenerExceptions() {
        return isIgnoreListenerExceptions;
    }

    /// Returns the handler to provide a default value when no value is found for a specified
    /// [key][LocalizationRequestSource.Key].
    ///
    /// Default: `(_, _, key) -> "[" + key + "]"` (e.g., `"[program.name]"`).
    /// @return Handler to provide a default value.
    /// @since 2.0
    public MissingValueHandler getMissingValueHandler() {
        return missingValueHandler;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof LocalizeConfig other)) return false;
        if (other == this) return true;
        return isThrowOnMissingValue == other.isThrowOnMissingValue
                && isIgnoreProviderExceptions == other.isIgnoreProviderExceptions
                && isIgnoreFormatterExceptions == other.isIgnoreFormatterExceptions
                && isIgnoreListenerExceptions == other.isIgnoreListenerExceptions
                && Objects.equals(missingValueHandler, other.missingValueHandler);
    }

    @Override public int hashCode() {
        return Objects.hash(
            isThrowOnMissingValue,
            isIgnoreProviderExceptions,
            isIgnoreFormatterExceptions,
            isIgnoreListenerExceptions,
            missingValueHandler
        );
    }
}
