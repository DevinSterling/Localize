package com.devinsterling.localize;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/// Formats a request into a formatted localized string.
///
/// The default formatter for [Localize] instances can be set using SPI,
/// avoiding explicit calls to [Localize#setFormatter].
/// For more details see
/// [`LocalizationFormatterProvider`][com.devinsterling.localize.spi.LocalizationFormatterProvider].
///
/// @see Localize#setFormatter
/// @see com.devinsterling.localize.spi.LocalizationFormatterProvider
/// @since 1.0
@FunctionalInterface
public interface LocalizationFormatter {
    /// Processes the given [Request] to retrieve a formatted localized string.
    ///
    /// @param request Formatter request.
    /// @return Formatted localized string or `null` if not found.
    /// @throws NullPointerException if `request` is `null`.
    String format(Request request);

    /// Returns the preferred arguments type, if any.
    ///
    /// | [Arguments.Type]            | Effect                  |
    /// |-----------------------------|-------------------------|
    /// | [Arguments.Type#NAMED]      | Prefer named arguments. |
    /// | [Arguments.Type#NONE]       | No Effect.              |
    /// | [Arguments.Type#POSITIONAL] | No Effect.              |
    /// | `null`                      | No Effect (default).    |
    ///
    /// ### Named Arguments Hint
    /// A hint of [Arguments.Type#NAMED] requests that positional arguments are internally stored as named arguments,
    /// using the positional index as the argument key.
    /// For example, if a [builder][LocalizationValueBuilder] receives
    /// [positional][LocalizationValueBuilder#arg(Object)] arguments, they are internally treated as named arguments
    /// using key-value pairs (e.g., key=`"0"`, value=`"argument"`).
    ///
    /// This is only an implementation detail of the backing data structure.
    /// Hints have no effect on [Arguments#type].
    ///
    /// @return Preferred arguments type or `null` if unspecified.
    /// @since 2.0
    default Arguments.Type argumentsHint() {
        return null;
    }

    /// Formatter request to produce a formatted localized values.
    ///
    /// **This is a value-based class**.
    /// Programmers should treat instances that are equal as interchangeable,
    /// avoid identity checks (`==`), and never use instances for synchronization,
    /// or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
    ///
    /// @see Builder#of(String)
    /// @since 2.0
    final class Request {
        private final String pattern;
        private final Arguments arguments;
        private final Locale locale;

        private Request(String pattern, Arguments arguments, Locale locale) {
            this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
            this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
            this.locale = Objects.requireNonNull(locale, "locale must not be null");
        }

        /// Returns the message pattern to format.
        ///
        /// @return Message pattern to format.
        public String getPattern() {
            return pattern;
        }

        /// Returns the arguments to format with.
        ///
        /// @return Arguments to format with.
        public Arguments getArguments() {
            return arguments;
        }

        /// Returns the locale to format by.
        ///
        /// @return Locale to format by.
        public Locale getLocale() {
            return locale;
        }

        // When value classes become stable, equals and hashcode will be removed here
        @Override public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Request other)) return false;
            return pattern.equals(other.pattern)
                    && arguments.equals(other.arguments)
                    && locale.equals(other.locale);
        }

        @Override public int hashCode() {
            return Objects.hash(pattern, arguments, locale);
        }

        /// Builder to create a [Request] for string formatting.
        ///
        /// @see Builder#of(String)
        static final class Builder {
            private String pattern;
            private Arguments arguments = Arguments.NONE;
            private Locale locale;

            /// Creates a builder instance with the given request.
            ///
            /// ### Note
            /// [`locale`][locale(Locale)] must be set before calling [build].
            /// Otherwise, an [NullPointerException] will be thrown.
            ///
            /// @return Builder instance.
            /// @throws NullPointerException If `request` is `null`.
            public static Builder of(String pattern) {
                return new Builder().pattern(pattern);
            }

            /// Sets the message pattern to format.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `pattern` is `null`.
            public Builder pattern(String pattern) {
                this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
                return this;
            }

            /// Sets the arguments to format with.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `arguments` is `null`.
            public Builder arguments(Arguments arguments) {
                this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
                return this;
            }

            /// Sets the locale to format by.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `locale` is `null`.
            public Builder locale(Locale locale) {
                this.locale = Objects.requireNonNull(locale, "locale must not be null");
                return this;
            }

            /// Builds a [Request] instance.
            ///
            /// @return Formatter request.
            /// @throws NullPointerException If [`locale`][locale(Locale)] is `null`.
            public Request build() {
                return new Request(this.pattern, this.arguments, this.locale);
            }
        }
    }

    /// The default formatter to handle converting a [Request]
    /// into a formatted localized string.
    LocalizationFormatter DEFAULT = new LocalizationFormatter() {
        @Override public String format(Request request) {
            String value = request.getPattern();
            Arguments arguments = request.getArguments();
            Object[] positionalArguments = null;

            if (!arguments.isEmpty()) {
                if (arguments.isPositional()) {
                    positionalArguments = arguments.toArray();
                } else if (arguments.isNamed()) {
                    positionalArguments = new Object[arguments.size()];
                    value = convertToPositionalArgs(value, arguments.toNamedMap(), positionalArguments);
                }

                value = new MessageFormat(value, request.getLocale()).format(positionalArguments);
            }

            return value;
        }

        private static String convertToPositionalArgs(
            String value,
            Map<String, Object> mapArguments,
            Object[] arguments
        ) {
            Map<String, Integer> keyToIndex = new HashMap<>();
            StringBuilder buf = new StringBuilder(value.length());
            boolean isQuoted = false;
            int insertAt = 0;
            int start = -1;

            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);

                if (start >= 0) {
                    if (c == '}' || c == ',') {
                        String key = value.substring(start, i);
                        // Before inserting, check if the key was visited before
                        Integer position = keyToIndex.get(key);

                        if (position == null) {
                            position = insertAt++;
                            keyToIndex.put(key, position);

                            if (position < arguments.length) {
                                arguments[position] = mapArguments.get(key);
                            }
                        }

                        buf.append(position);
                        start = -1;
                    } else {
                        continue;
                    }
                } else if (c == '\'') {
                    isQuoted = !isQuoted;
                } else if (!isQuoted && c == '{') {
                    start = i + 1;
                }

                buf.append(c);
            }

            // If there's an unclosed brace, add the content after
            if (start > 0) {
                buf.append(value, start, value.length());
            }

            return buf.toString();
        }
    };
}
