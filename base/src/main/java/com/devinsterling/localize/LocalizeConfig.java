package com.devinsterling.localize;

import java.util.MissingResourceException;

/// Configuration to change how [Localize] handles operations.
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

    /// Instantiate a configuration instance
    /// with all values set to their defaults.
    public LocalizeConfig() {}

    /// When set to `true`, a [MissingResourceException] is thrown
    /// when:
    /// - **All** bundles contain no value for a specified key.
    /// - [LocalizationRequest#getDefaultValue()] is `null`
    ///   (Set by [LocalizationValueBuilder#defaultValue(String)]).
    ///
    /// The initial value is `false`.
    ///
    /// @param isThrowWhenNoValueFound Flag to throw an exception.
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

    /// The default value to return if no value for a specified key was found.
    ///
    /// The initial value is an empty string.
    ///
    /// @param defaultMissingValue Default value for missing pairings.
    public void setDefaultMissingValue(String defaultMissingValue) {
        this.defaultMissingValue = defaultMissingValue;
    }

    /// {@return `true`, if an exception is to be thrown.}
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

    /// {@return Default value for missing pairings.}
    public String getDefaultMissingValue() {
        return defaultMissingValue;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof LocalizeConfig config)) return false;
        if (config == this) return true;
        return this.isIgnoreMissingResourceBundles == config.isIgnoreMissingResourceBundles
                && this.isIgnoreProcessingExceptions == config.isIgnoreProcessingExceptions
                && this.isThrowWhenNoValueFound == config.isThrowWhenNoValueFound
                && this.defaultMissingValue.equals(config.defaultMissingValue);
    }
}
