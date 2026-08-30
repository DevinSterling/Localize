package com.devinsterling.localize;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/// Formats a request into a formatted localized string.
///
/// @see Localize#setFormatter
/// @since 1.0
@FunctionalInterface
public interface LocalizationFormatter {
    /// Processes the given [Context] to retrieve a formatted localized string.
    ///
    /// @param context Formatter context.
    /// @return Formatted localized string or `null` if not found.
    String format(Context context);

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

    /// Processor context to assist creation of formatted localized values.
    ///
    /// @see Builder#of(String)
    /// @since 2.0
    final class Context {
        private final String pattern;
        private final Arguments arguments;
        private final Locale locale;

        private Context(String pattern, Arguments arguments, Locale locale) {
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

        /// Builder to create a [Context] for string formatting.
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

            /// Builds a [Context] instance.
            ///
            /// @return Formatter context.
            /// @throws NullPointerException If [`locale`][locale(Locale)] is `null`.
            public Context build() {
                return new Context(this.pattern, this.arguments, this.locale);
            }
        }
    }

    /// The default formatter to handle converting a [Context]
    /// into a formatted localized string.
    LocalizationFormatter DEFAULT = new LocalizationFormatter() {
        @Override public String format(Context ctx) {
            String value = ctx.getPattern();
            Arguments arguments = ctx.getArguments();
            Object[] positionalArguments = null;

            if (!arguments.isEmpty()) {
                if (arguments.isPositional()) {
                    positionalArguments = arguments.toArray();
                } else if (arguments.isNamed()) {
                    positionalArguments = new Object[arguments.size()];
                    value = convertToPositionalArgs(value, arguments.toNamedMap(), positionalArguments);
                }
            }

            return new MessageFormat(value, ctx.getLocale()).format(positionalArguments);
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
