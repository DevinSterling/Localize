package com.devinsterling.localize;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/// Formats a [request][Request] into a localized string.
///
/// The default formatter for [Localize] instances can be set using SPI,
/// avoiding explicit calls to [Localize#setFormatter].
/// For more details see
/// [`LocalizationFormatterProvider`][com.devinsterling.localize.spi.LocalizationFormatterProvider].
///
/// If no formatter is specified via SPI, [STANDARD] is used as the default for [Localize] instances.
///
/// @see Localize#setFormatter
/// @see com.devinsterling.localize.spi.LocalizationFormatterProvider
/// @since 1.0
@FunctionalInterface
public interface LocalizationFormatter {
    /// Processes the given [Request] to retrieve a formatted localized string.
    ///
    /// @param request Formatter request.
    /// @return Formatted localized string.
    /// @throws NullPointerException if `request` is `null`.
    /// @implSpec This method must be thread-safe.
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
    /// @see Builder#of
    /// @since 2.0
    final class Request {
        private final Localize localize;
        private final Locale locale;
        private final String pattern;
        private final Arguments arguments;

        private Request(Localize localize, Locale locale, String pattern, Arguments arguments) {
            // A null check is not required here as that's handled by the builder
            this.localize = localize;
            this.locale = locale;
            this.pattern = pattern;
            this.arguments = arguments;
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

        /// Returns the source Localize instance where the request originates from.
        ///
        /// The source instance allows formatters to perform advanced operations,
        /// such as resolving nested keys within patterns.
        ///
        /// ### Recursion
        /// When recursively resolving keys, implementers are recommended to guard against circular references
        /// (e.g., Key A references Key B, and vice versa) to prevent a [StackOverflowError].
        ///
        /// @return Source Localize instance.
        public Localize getLocalize() {
            return localize;
        }

        // When value classes become stable, equals and hashcode will be removed here
        @Override public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Request other)) return false;
            return localize.equals(other.localize)
                    && locale.equals(other.locale)
                    && pattern.equals(other.pattern)
                    && arguments.equals(other.arguments);
        }

        @Override public int hashCode() {
            return Objects.hash(localize, locale, pattern, arguments);
        }

        /// Builder to create a [Request] for string formatting.
        ///
        /// @see Builder#of
        public static final class Builder {
            private Localize localize;
            private Locale locale;
            private String pattern;
            private Arguments arguments = Arguments.NONE;

            private Builder() {}

            /// Creates a builder instance with the given request.
            ///
            /// By default, the [`locale`][locale(Locale)] is set to [Localize#getLocale].
            ///
            /// @param localize Source Localize instance.
            /// @param pattern  Message pattern to format.
            /// @return         Builder instance.
            /// @throws NullPointerException If `localize` or `pattern` is `null`.
            public static Builder of(Localize localize, String pattern) {
                return new Builder()
                    .localize(localize)
                    .locale(localize.getLocale())
                    .pattern(pattern);
            }

            /// Sets the message pattern to format.
            ///
            /// @param pattern Message pattern to format.
            /// @return        This builder instance.
            /// @throws NullPointerException If `pattern` is `null`.
            public Builder pattern(String pattern) {
                this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
                return this;
            }

            /// Sets the arguments to format with.
            ///
            /// @param arguments Arguments to format with.
            /// @return          This builder instance.
            /// @throws NullPointerException If `arguments` is `null`.
            public Builder arguments(Arguments arguments) {
                this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
                return this;
            }

            /// Sets the locale to format by.
            ///
            /// @param locale Locale to format by
            /// @return       This builder instance.
            /// @throws NullPointerException If `locale` is `null`.
            public Builder locale(Locale locale) {
                this.locale = Objects.requireNonNull(locale, "locale must not be null");
                return this;
            }

            /// Sets the source [Localize] instance, indicating where requests originate from.
            ///
            /// @param localize Source localize instance.
            /// @return         This builder instance.
            /// @throws NullPointerException If `localize` is `null`.
            public Builder localize(Localize localize) {
                this.localize = Objects.requireNonNull(localize, "localize must not be null");
                return this;
            }

            /// Builds a [Request] instance.
            ///
            /// @return Formatter request.
            public Request build() {
                return new Request(localize, locale, pattern, arguments);
            }
        }
    }

    /// The standard formatter to format a [Request] into a localized string.
    ///
    /// The standard formatter is built around [MessageFormat] and supports all of its syntax,
    /// including pluralization through [java.text.ChoiceFormat] choice patterns.
    /// In addition, both named and numbered arguments are supported.
    ///
    /// > For advanced message formatting, plural rules, and greater control over bundle properties,
    /// see the [ICU4J integration module](https://github.com/DevinSterling/Localize#icu4j-integration),
    /// an optional dependency (`localize-icu4j`) providing
    /// [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/#platform-dependencies) support.
    ///
    /// Examples within a properties file using [MessageFormat] syntax:
    /// ```properties
    /// Example.simple=Hello World!
    ///
    /// Example.numberedArguments=Hello {0}! Welcome to {1}, {0}!
    ///
    /// Example.namedArguments=Yesterday was {yesterday_date} and tomorrow is {tomorrow_date}.
    ///
    /// Example.quoted=To use a single quote, it must be escaped. '' will appear as a single quote.
    ///
    /// Example.pluralization={name} clicked this button {click_count, choice,\
    /// 0 #zero times|\
    /// 1 #one time|\
    /// 1 <{click_count} times}!
    /// ```
    ///
    /// ### Named Arguments
    /// For compatibility with [MessageFormat], which uses numbered arguments,
    /// named arguments are converted into numbered arguments.
    /// For example, `Hi {first} {last}` becomes `Hi {0} {1}`.
    ///
    /// Named arguments are converted to numbered arguments only [when named arguments are present][Arguments#isNamed].
    ///
    /// The following characters are not allowed within named argument keys: `{`, `}`, `,`
    ///
    /// #### Malformed Named Argument Syntax
    /// Nested or unclosed braces (e.g., `{x{y}}`, `{x} {y`) are not valid syntax.
    /// The underlying [MessageFormat] will throw an [IllegalArgumentException].
    ///
    /// @implNote As an optimization, requests with no arguments and no quotes to unescape in the pattern
    ///           return the pattern as-is without invoking [MessageFormat].
    /// @since 2.0
    LocalizationFormatter STANDARD = new LocalizationFormatter() {
        @Override public String format(Request request) {
            String value = request.getPattern();
            Arguments arguments = request.getArguments();
            boolean hasArguments = !arguments.isEmpty();

            // Return early; nothing for `MessageFormat` to do as there are no arguments or quotes to unescape.
            if (!hasArguments && value.indexOf('\'') < 0) {
                return value;
            }

            Object[] positionalArguments = null;

            // Check for '{' even if args are empty to avoid throwing if a named key is referenced
            if (hasArguments || value.indexOf('{') >= 0) {
                if (arguments.isPositional()) {
                    positionalArguments = arguments.toArray();
                } else {
                    List<Object> positional = new ArrayList<>(arguments.size());
                    value = convertToPositionalArgs(value, arguments.toNamedMap(), positional);
                    positionalArguments = positional.toArray();
                }
            }

            return new MessageFormat(value, request.getLocale()).format(positionalArguments);
        }

        private static String convertToPositionalArgs(
            String raw,
            Map<String, Object> source,
            List<Object> arguments
        ) {
            Map<String, Integer> keyToIndex = new HashMap<>();
            StringBuilder buf = new StringBuilder(raw.length());
            boolean isQuoted = false;
            int insertAt = 0;
            int start = -1;

            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);

                if (start >= 0) switch (c) {
                    case '}', ',' -> {
                        String key = checkBlank(raw.substring(start, i));
                        start = -1;
                        // Before inserting, check if the key was visited before
                        Integer position = keyToIndex.get(key);

                        if (position == null) {
                            position = insertAt++;
                            keyToIndex.put(key, position);
                            // If the source does not contain the key, use a placeholder value
                            arguments.add(source.containsKey(key) ? source.get(key) : "{" + key + "}");
                        }
                        buf.append(position);

                        // If the key is missing from the source, collapse the sub-pattern
                        if (c == ',' && !source.containsKey(key)) {
                            i = skipToMatchingClosingBrace(i, raw);
                            buf.append("}");
                            continue;
                        }
                    }
                    // NOTE: If this case happens, it's a bug from the end-caller since `{0{1}}` is invalid syntax.
                    //       `MessageFormat` would throw if not thrown here.
                    case '{' -> throw new IllegalArgumentException(
                        "Can't parse argument at index " + (start - 1) +
                        ": " + raw.substring(start, skipToMatchingClosingBrace(i, raw))
                    );
                    default -> {
                        continue;
                    }
                } else if (c == '\'') {
                    isQuoted = !isQuoted;
                } else if (!isQuoted && c == '{') {
                    start = i + 1;
                }

                buf.append(c);
            }

            // If there's an unclosed brace, throw an exception
            throwIfMissingBrace(start > 0);

            return buf.toString();
        }

        private static int skipToMatchingClosingBrace(int i, String s) {
            boolean isQuoted = false;
            int depth = 1;

            while (i < s.length()) {
                char c = s.charAt(i);

                if (c == '\'') {
                    isQuoted = !isQuoted;
                } else if (!isQuoted && c == '{') {
                    depth++;
                } else if (!isQuoted && c == '}' && --depth == 0) {
                    break;
                }
                i++;
            }

            throwIfMissingBrace(isQuoted || depth > 0);

            return i;
        }

        public static String checkBlank(String key) {
            // Just like native positional arguments, throw if no argument reference is present (e.g., "{}", "{ }")
            if (key.isBlank()) {
                throw new IllegalArgumentException("Can't parse blank argument: {}");
            }

            return key;
        }

        public static void throwIfMissingBrace(boolean isMissingBrace) {
            if (isMissingBrace) {
                throw new IllegalArgumentException("Unmatched braces in the pattern.");
            }
        }
    };
}
