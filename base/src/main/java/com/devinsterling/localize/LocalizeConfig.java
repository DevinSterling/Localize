package com.devinsterling.localize;

import java.util.MissingResourceException;
import java.util.Objects;

/// Configuration to control how [Localize] handles operations.
///
/// ### Default Configuration
/// - [isThrowWhenNoValueFound][LocalizeConfig#setThrowWhenNoValueFound] = `false`
/// - [isIgnoreProcessingException][LocalizeConfig#setIgnoreProcessingExceptions] = `false`
/// - [isIgnoreMissingResourceBundles][LocalizeConfig#setIgnoreMissingResourceBundles] = `false`
/// - [defaultMissingValue][LocalizeConfig#setDefaultMissingValue] = `""`
///
/// @since 1.0
public class LocalizeConfig {
    private volatile boolean isThrowWhenNoValueFound = false;
    private volatile boolean isIgnoreProcessingExceptions = false;
    private volatile boolean isIgnoreMissingResourceBundles = false;
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

    /// When set to `true`, this will ignore all runtime exceptions that
    /// occur from processing a resource bundle. The next bundle enqueued
    /// will be processed as if nothing happened.
    ///
    /// The initial value is `false`.
    ///
    /// @param isIgnoreProcessingExceptions Flag to ignore runtime exceptions.
    public void setIgnoreProcessingExceptions(boolean isIgnoreProcessingExceptions) {
        this.isIgnoreProcessingExceptions = isIgnoreProcessingExceptions;
    }

    /// When set to `true`, this will ignore all runtime exceptions that
    /// occur from when a resource bundle is not found; missing.
    ///
    /// The initial value is `false`.
    ///
    /// @param isIgnoreMissingResourceBundles Flag to ignore runtime exceptions.
    public void setIgnoreMissingResourceBundles(boolean isIgnoreMissingResourceBundles) {
        this.isIgnoreMissingResourceBundles = isIgnoreMissingResourceBundles;
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

    /// {@return `true`, if runtime exceptions must be ignored.}
    public boolean isIgnoreProcessingExceptions() {
        return isIgnoreProcessingExceptions;
    }

    /// {@return `true`, if missing resource bundles are ignored.}
    public boolean isIgnoreMissingResourceBundles() {
        return isIgnoreMissingResourceBundles;
    }

    /// Returns the default value to use when no value is found for a specified key.
    ///
    /// Default: `""` (Empty string)
    /// @return Default value.
    public String getDefaultMissingValue() {
        return defaultMissingValue;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof LocalizeConfig config)) return false;
        if (config == this) return true;
        return this.isIgnoreMissingResourceBundles == config.isIgnoreMissingResourceBundles
                && this.isIgnoreProcessingExceptions == config.isIgnoreProcessingExceptions
                && this.isThrowWhenNoValueFound == config.isThrowWhenNoValueFound
                && Objects.equals(this.defaultMissingValue, config.defaultMissingValue);
    }

    @Override public int hashCode() {
        return Objects.hash(
            isIgnoreMissingResourceBundles,
            isIgnoreProcessingExceptions,
            isThrowWhenNoValueFound,
            defaultMissingValue
        );
    }
}
