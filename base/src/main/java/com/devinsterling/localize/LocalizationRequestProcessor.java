package com.devinsterling.localize;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/// Processes a request to provide a formatted localized string.
///
/// @see Localize#setProcessor
/// @since 1.0
@FunctionalInterface
public interface LocalizationRequestProcessor {
    /// Processes the given [Context] to retrieve a formatted localized string.
    ///
    /// @param context Processor context.
    /// @return Formatted localized string or `null` if not found.
    String process(Context context);

    /// Processor context to assist creation of formatted localized values.
    ///
    /// @see Builder#of(LocalizationRequest)
    /// @since 2.0
    final class Context {
        private final Locale locale;
        private final ResourceBundle bundle;
        private final LocalizationRequest request;

        private Context(Locale locale, ResourceBundle bundle, LocalizationRequest request) {
            this.locale = Objects.requireNonNull(locale, "locale must not be null");
            this.bundle = Objects.requireNonNull(bundle, "bundle must not be null");
            this.request = Objects.requireNonNull(request, "request must not be null");
        }

        /// Returns the locale to format by.
        ///
        /// ### Note
        /// This method *should* be preferred over [ResourceBundle#getLocale]
        /// as bundles can return a different locale.
        ///
        /// @return Locale to format by.
        public Locale getLocale() {
            return locale;
        }

        /// Returns the resource bundle to perform lookup on.
        ///
        /// ### Note
        /// [#getLocale] is strongly preferred over [ResourceBundle#getLocale].
        ///
        /// @return Resource bundle for lookup.
        public ResourceBundle getBundle() {
            return bundle;
        }

        /// Returns the request to get and format a value by.
        ///
        /// @return Localization request.
        public LocalizationRequest getRequest() {
            return request;
        }

        /// Builder to create a [Context] for string formatting.
        ///
        /// @see Builder#of(LocalizationRequest)
        /// @since 2.0
        static final class Builder {
            private Locale locale;
            private ResourceBundle bundle;
            private LocalizationRequest request;

            /// Creates a builder instance with the given request.
            ///
            /// ### Note
            /// [`bundle`][bundle(ResourceBundle)] and [`locale`][locale(Locale)] must be set
            /// before calling [build]. Otherwise, an [NullPointerException] will be thrown.
            ///
            /// @return Builder instance.
            /// @throws NullPointerException If `request` is `null`.
            public static Builder of(LocalizationRequest request) {
                return new Builder().request(request);
            }

            /// Sets the locale to format by.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `locale` is `null`.
            public Builder locale(Locale locale) {
                this.locale = Objects.requireNonNull(locale, "locale must not be null");
                return this;
            }

            /// Sets the resource bundle to perform lookup on.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `bundle` is `null`.
            public Builder bundle(ResourceBundle bundle) {
                this.bundle = Objects.requireNonNull(bundle, "bundle must not be null");
                return this;
            }

            /// Sets the localization request.
            ///
            /// @return This builder instance.
            /// @throws NullPointerException If `request` is `null`.
            public Builder request(LocalizationRequest request) {
                this.request = Objects.requireNonNull(request, "request must not be null");
                return this;
            }

            /// Builds a [Context] instance.
            ///
            /// @return Processor context.
            /// @throws NullPointerException If [`request`][request(LocalizationRequest)],
            /// [`bundle`][bundle(ResourceBundle)], or [`locale`][locale(Locale)] is `null`.
            public Context build() {
                return new Context(this.locale, this.bundle, this.request);
            }
        }
    }
}
