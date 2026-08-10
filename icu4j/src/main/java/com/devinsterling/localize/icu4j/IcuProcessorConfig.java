package com.devinsterling.localize.icu4j;

import java.util.Objects;

/// Configuration for [IcuProcessor] instances.
///
/// ### Default Configuration
/// | Method                            | Property                   | Default                                                                     |
/// |-----------------------------------|----------------------------|-----------------------------------------------------------------------------|
/// | [IcuProcessorConfig#setFormatter] | `localize.icu4j.formatter` | `com.ibm.icu.message2.MessageFormatter` ([IcuFormatter#MESSAGE2_FORMATTER]) |
///
/// ### Properties and Environmental Variables
/// Properties are configurable by placing `localize.properties` in the classpath/resources
/// or dynamically via [System#setProperty].
/// Changing properties during runtime only affects newly created instances.
///
/// @since 2.0
public class IcuProcessorConfig {
    private volatile IcuFormatter formatter;

    /// Creates a configuration instance with all values set to their defaults.
    public IcuProcessorConfig() {
        formatter = IcuProperties.INSTANCE.getFormatterType();
    }

    /// Returns the ICU4J message formatter.
    ///
    /// @return ICU4J message formatter version.
    public IcuFormatter getFormatter() {
        return formatter;
    }

    /// Sets which ICU4J message formatter to use.
    ///
    /// ### Override Default
    /// The default version can be overridden within `localize.properties` or system properties
    /// by setting `localize.icu4j.formatter` to one of the following values:
    /// - `com.ibm.icu.message2.MessageFormatter` (default)
    /// - `com.ibm.icu.text.MessageFormat` (legacy)
    ///
    /// @param formatter The ICU4J message formatter.
    /// @throws NullPointerException If `formatter` is `null`.
    public void setFormatter(IcuFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter,  "formatter must not be null");
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IcuProcessorConfig config)) return false;
        return this.formatter == config.formatter;
    }

    @Override public int hashCode() {
        return formatter.hashCode();
    }
}
