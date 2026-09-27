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
/// - [isThrowWhenNoValueFound][LocalizeConfig#setThrowWhenNoValueFound] = `false`
/// - [isIgnoreFormatterExceptions][setIgnoreFormatterExceptions] = `false`
/// - [isIgnoreProviderExceptions][setIgnoreProviderExceptions] = `false`
/// - [isIgnoreListenerExceptions][setIgnoreListenerExceptions] = `false`
/// - [defaultMissingValue][LocalizeConfig#setDefaultMissingValue] = `""`
///
/// @since 1.0
public class LocalizeConfig {
    private volatile boolean isThrowWhenNoValueFound = false;
    private volatile boolean isIgnoreFormatterExceptions = false;
    private volatile boolean isIgnoreProviderExceptions = false;
    private volatile boolean isIgnoreListenerExceptions = false;
    private volatile String defaultMissingValue = "";

    /// Creates a configuration instance with all values set to their defaults.
    public LocalizeConfig() {}

    /// Sets whether to throw a [MissingResourceException] when no value is found for a specified key.
    ///
    /// When set to `true`, a [MissingResourceException] is thrown when all the conditions are met:
    /// 1. **All** bundles contain no value for a specified key.
    /// 2. [LocalizationRequest#getDefaultValue] is `null` (Set by [LocalizationValueBuilder#defaultValue(String)]).
    ///
    /// Default: `false`
    /// @param isThrowWhenNoValueFound `true` to throw an exception, or `false` to ignore.
    public void setThrowWhenNoValueFound(boolean isThrowWhenNoValueFound) {
        this.isThrowWhenNoValueFound = isThrowWhenNoValueFound;
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

    /// Sets the default value to use when no value is found for a specified key.
    ///
    /// [LocalizationRequest#getDefaultValue()] takes precedence over the default value set here.
    /// If it is `null`, then default value set here is used.
    ///
    /// Default: `""` (Empty string)
    /// @param defaultValue Default value.
    public void setDefaultMissingValue(String defaultValue) {
        this.defaultMissingValue = defaultValue;
    }

    /// Returns `true` if a [MissingResourceException] is thrown when no value is found for a specified key.
    ///
    /// Default: `false`
    /// @return `true` if an exception is thrown, or `false` if ignored.
    public boolean isThrowWhenNoValueFound() {
        return isThrowWhenNoValueFound;
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

    /// Returns the default value to use when no value is found for a specified key.
    ///
    /// Default: `""` (Empty string)
    /// @return Default value.
    public String getDefaultMissingValue() {
        return defaultMissingValue;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof LocalizeConfig other)) return false;
        if (other == this) return true;
        return isIgnoreProviderExceptions == other.isIgnoreProviderExceptions
                && isIgnoreFormatterExceptions == other.isIgnoreFormatterExceptions
                && isIgnoreListenerExceptions == other.isIgnoreListenerExceptions
                && isThrowWhenNoValueFound == other.isThrowWhenNoValueFound
                && Objects.equals(defaultMissingValue, other.defaultMissingValue);
    }

    @Override public int hashCode() {
        return Objects.hash(
            isIgnoreProviderExceptions,
            isIgnoreFormatterExceptions,
            isIgnoreListenerExceptions,
            isThrowWhenNoValueFound,
            defaultMissingValue
        );
    }
}
