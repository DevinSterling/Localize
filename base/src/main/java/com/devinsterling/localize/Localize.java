package com.devinsterling.localize;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/// Base class to handle localization.
///
/// It is recommended to create a thread-safe [Localize]
/// instance through the static factory methods listed here:
/// - [#of()]
/// - [#of(Locale)]
/// - [#of(LocalizeConfig)]
/// - [#of(Locale, LocalizeConfig)]
///
/// ### Arguments and Pluralization
/// By default, Localize uses a [LocalizationFormatter] built around [java.text.MessageFormat],
/// though this can be replaced [programmatically][setFormatter] or via SPI by providing a custom formatter.
/// The default formatter supports both named and numbered arguments,
/// as well as pluralization through [java.text.ChoiceFormat] choice patterns.
///
/// > For advanced message formatting, plural rules, and greater control over bundle properties,
/// see the [ICU4J integration module](https://github.com/DevinSterling/Localize#icu4j-integration),
/// an optional dependency (`localize-icu4j`) providing
/// [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/#platform-dependencies) support.
///
/// ### Example
/// Localization `*.properties` files reside under `resources`.
///
/// For example, such files residing in a subdirectory `sample`:
/// - `sample/message.properties`
/// - `sample/message_en_US.properties`
/// - `sample/message_zh_CN.properties`
/// - `sample/message_ja.properties`
///
/// Here is a look inside the contents of `message_en_US.properties`:
/// ```properties
/// MyApp.greet = Good Morning
/// // Numbered arguments
/// MyApp.say1 = {0}, {1}
/// // Named arguments (Argument insertion order does not matter)
/// MyApp.say2 = {intro}, {name}
/// // Pluralization and named arguments
/// MyApp.people = There {num_people, choice,\
/// 0 #are no people|\
/// 1 #is one person|\
/// 1 <are {num_people} people} on {location}.
/// ```
///
/// The following localization files may be called as such:
/// ```java
/// Localize localize = Localize.of();
/// localize.addBundleProvider("sample.message");
/// // Or alternatively for more control
/// localize.addBundleProvider(locale -> ResourceBundle.getBundle("sample.message", locale));
///
/// // We may now change the locale a number of times.
/// localize.setLocale(Locale.CHINESE);
/// assert localize.getValue("MyApp.greet").equals("早上好");
///
/// localize.setLocale(Locale.JAPANESE);
/// assert localize.getValue("MyApp.greet").equals("おはよう");
///
/// localize.setLocale(Locale.ENGLISH);
/// assert localize.get("MyApp.say1")
///                .arg("Hi")
///                .arg("Devin")
///                .value()
///                .equals("Hi, Devin");
///
/// assert localize.get("MyApp.say2")
///                .arg("name", "Devin")
///                .arg("intro", "Hi")
///                .value()
///                .equals("Hi, Devin");
///
/// assert localize.get("MyApp.people")
///                .arg("location", "campus")
///                .arg("num_people", 100)
///                .value()
///                .equals("There are 100 people on campus.");
/// ```
/// @implSpec Implementations must ensure that locale updates are thread-safe.
/// @since 1.0
public abstract class Localize {
    private final ProviderStore providerStore = new ProviderStore();
    private final Object providerLock = new Object();
    private final LocalizeConfig config;
    private volatile LocalizationFormatter formatter = LocalizationFormatterLocator.PROVIDER.provide();

    /// Creates a [Localize] instance with the desired configuration.
    ///
    /// @param config The configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected Localize(LocalizeConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /// Sets the locale and updates all resource bundles.
    ///
    /// Changing the locale will trigger a [#refresh()]
    ///
    /// @param locale Locale to fetch associated resource bundles.
    /// @throws NullPointerException If locale is `null`.
    public abstract void setLocale(Locale locale);

    /// The current locale.
    ///
    /// @return The current locale.
    public abstract Locale getLocale();

    /// Equivalent to [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** Localize instance.
    public static Localize of() {
        return of(Locale.getDefault());
    }

    /// Equivalent to [#of(Locale, LocalizeConfig)] with a given
    /// [Locale] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** Localize instance.
    /// @throws NullPointerException If `locale` is `null`.
    public static Localize of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Equivalent to [#of(Locale, LocalizeConfig)] with a given
    /// [LocalizeConfig] and initial locale set as [Locale#getDefault()].
    ///
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** Localize instance.
    /// @throws NullPointerException If `config` is `null`.
    /// @since 1.2
    public static Localize of(LocalizeConfig config) {
        return of(Locale.getDefault(), config);
    }

    /// Creates a new [Localize] instance with the given [Locale] and [LocalizeConfig].
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** Localize instance.
    /// @throws NullPointerException If `locale` or `config` is `null`.
    public static Localize of(Locale locale, LocalizeConfig config) {
        return new LocalizeImpl(assertLocale(locale), config);
    }

    /// Sets the localization formatter.
    ///
    /// The formatter is called each time a request is made to format a value.
    ///
    /// @param formatter Formatter to format requests.
    /// @throws NullPointerException If `formatter` is `null`.
    public void setFormatter(LocalizationFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "Formatter must not be null");
    }

    /// {@return The localization formatter}
    public LocalizationFormatter getFormatter() {
        return formatter;
    }

    /// {@return The localize configuration}
    public LocalizeConfig getConfig() {
        return config;
    }

    /// Adds the given provider to retrieve localized values from.
    ///
    /// The provider is called each time the locale changes to fetch the corresponding [ResourceBundle].
    ///
    /// ### Precedence
    /// Providers are prioritized in the order they are added.
    /// Providers added earlier have higher priority than those added later (e.g., fallbacks).
    /// Replacing an existing provider with the given key does not change its priority.
    ///
    /// If multiple providers have the same resource property keys,
    /// the value from the highest-priority provider is used.
    ///
    /// ### Example Usage
    /// ```java
    /// localize.putBundleProvider("myKey", locale -> {
    ///     return ResourceBundle.getBundle("i18n.messages", locale);
    /// });
    ///
    /// // Has lower priority than the provider put first
    /// // (i.e., Localized value requests)
    /// localize.putBundleProvider("myKey2", locale -> {
    ///     return ResourceBundle.getBundle("i18n.other", locale);
    /// });
    ///
    /// // Removal
    /// localize.removeBundleProvider("myKey");
    /// ```
    ///
    /// @param key      Key associated with `provider`.
    /// @param provider Called upon calling refresh to get a ResourceBundle instance.
    /// @return         `true` if the key had no association prior. Otherwise, `false` is
    ///                 returned when the previous entry is replaced with the new provider.
    /// @throws NullPointerException If `key` or `provider` is `null`.
    /// @see putBundleProvider(String, String)
    /// @see addBundleProvider(ResourceBundleProvider)
    public boolean putBundleProvider(String key, ResourceBundleProvider provider) {
        ProviderEntry entry = new ProviderEntry(key, provider);
        boolean isNewEntry = providerStore.put(entry);
        refresh(entry);
        return isNewEntry;
    }

    /// Shorthand for [#putBundleProvider(String, ResourceBundleProvider)] using [ResourceBundle#getBundle(String)].
    ///
    /// This method is equivalent to:
    /// ```
    /// putBundleProvider(key, locale -> ResourceBundle.getBundle(baseName, locale));
    /// ```
    /// @param key                    Key associated with `provider`.
    /// @param resourceBundleBaseName Called upon calling refresh to get a ResourceBundle instance.
    /// @return                       `true` if the key had no association prior. Otherwise, `false` is
    ///                               returned when the previous entry is replaced with the new provider.
    /// @throws NullPointerException If `key` or `provider` is `null`.
    /// @see putBundleProvider(String, ResourceBundleProvider)
    /// @see addBundleProvider(String)
    /// @since 2.0
    public boolean putBundleProvider(String key, String resourceBundleBaseName) {
        return putBundleProvider(key, locale -> ResourceBundle.getBundle(resourceBundleBaseName, locale));
    }

    /// Adds the given provider and returns the generated unique key linked to it.
    ///
    /// This method is equivalent to [#putBundleProvider(String, ResourceBundleProvider)]
    /// without the need to manually specify a key.
    ///
    /// @apiNote        It is recommended to **not** make any assumptions on the length or format
    ///                 of the returned generated unique key as it could change in between versions.
    /// @param provider Called upon calling refresh to get a ResourceBundle instance.
    /// @return         The generated key, if needed for calls to [#removeBundleProvider(String)] or [#refresh(String)].
    /// @throws NullPointerException If `provider` is `null`.
    /// @see addBundleProvider(String)
    /// @see putBundleProvider(String, ResourceBundleProvider)
    /// @since 1.3
    public String addBundleProvider(ResourceBundleProvider provider) {
        ProviderEntry entry;
        String uniqueKey;

        synchronized (providerStore) {
            // Ensure the key is unique.
            // NOTE: In nearly every single case there is only 1 iteration
            do {
                uniqueKey = UUID.randomUUID().toString();
            } while (providerStore.get(uniqueKey) != null);

            entry = new ProviderEntry(uniqueKey, provider);
            providerStore.add(entry);
        }

        refresh(entry);
        return uniqueKey;
    }

    /// Shorthand for [#addBundleProvider(ResourceBundleProvider)] using [ResourceBundle#getBundle(String)].
    ///
    /// This method is equivalent to:
    /// ```
    /// addBundleProvider(locale -> ResourceBundle.getBundle(baseName, locale));
    /// ```
    /// @param resourceBundleBaseName Resource bundle base name.
    /// @return                       The generated key, if needed for calls to
    ///                               [#removeBundleProvider(String)] or [#refresh(String)].
    /// @throws NullPointerException If `provider` is `null`.
    /// @see addBundleProvider(ResourceBundleProvider)
    /// @see putBundleProvider(String, String)
    /// @since 2.0
    public String addBundleProvider(String resourceBundleBaseName) {
        return addBundleProvider(locale -> ResourceBundle.getBundle(resourceBundleBaseName, locale));
    }

    /// Removes the [ResourceBundleProvider] associated with the given key.
    ///
    /// @param key Key associated with the provider to remove.
    /// @return    `true` if the provider was removed.
    public boolean removeBundleProvider(String key) {
        return providerStore.remove(key);
    }

    /// Triggers a refresh for the specified provider to fetch a new [ResourceBundle].
    ///
    /// Useful for reloading a specific bundle from an external source (e.g., disk)
    /// after its contents have changed during runtime.
    ///
    /// @param key Key associated with the provider to refresh.
    /// @return    `true` if the provider was refreshed.
    ///            Otherwise, `false` is returned if the provider was not found.
    /// @see #putBundleProvider(String, ResourceBundleProvider)
    public boolean refresh(String key) {
        ProviderEntry entry = providerStore.get(key);
        boolean isFound = entry != null;

        if (isFound) {
            refresh(entry);
        }

        return isFound;
    }

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances.
    ///
    /// Useful for reloading bundles from external sources (e.g., disk)
    /// after their contents have changed during runtime.
    public void refresh() {
        refresh(getLocale());
    }

    /// Returns a builder for formatting a localized value from the given pattern.
    ///
    /// ### Example Usage
    /// Using a pattern format with positional arguments:
    /// ```
    /// String value = localize.format("Hello {0} {1}!")
    ///                        .arg("John")
    ///                        .arg("Doe")
    ///                        .value();
    ///
    /// assert value.equals("Hello John Doe!");
    /// ```
    /// @param pattern Pattern used to derive the formatted localized value.
    /// @return        **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `pattern` is `null`.
    /// @see LocalizationValueBuilder#value
    /// @see get(String)
    /// @since 2.0
    public LocalizationValueBuilder<?> format(String pattern) {
        return new LocalizationValueBuilder<>(new LocalizationRequestSource.Pattern(pattern), this);
    }

    /// Returns a builder for formatting a localized value retrieved from the given resource key.
    ///
    /// ### Example Usage
    /// Within a resource bundle (e.g., `my-app-i18n.properties`):
    /// ```
    /// MyApp.greet=Hello {first} {last}!
    /// ```
    /// Requesting the resource value by key:
    /// ```
    /// String value = localize.get("MyApp.greet")
    ///                        .arg("first", "John")
    ///                        .arg("last", "Doe")
    ///                        .value();
    ///
    /// assert value.equals("Hello John Doe!");
    /// ```
    /// @param key Key associated with the resource value to retrieve.
    /// @return    **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `key` is `null`.
    /// @see LocalizationValueBuilder#value
    /// @see format
    public LocalizationValueBuilder<?> get(String key) {
        return new LocalizationValueBuilder<>(new LocalizationRequestSource.Key(key), this);
    }

    /// Equivalent to [#get(String)].
    ///
    /// @param key Key associated with the resource value to retrieve.
    /// @return    **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `key` is `null`.
    /// @see LocalizationValueBuilder#value
    public LocalizationValueBuilder<?> get(LocalizationKey key) {
        return get(key.getKey());
    }

    /// Retrieves the value associated with a resource bundle key.
    ///
    /// @param key Key associated with the resource value to retrieve.
    /// @return    Resource bundle value or an empty string if not found.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(LocalizationKey)
    public String getValue(String key) {
        return get(key).value();
    }

    /// Equivalent to [#getValue(String)].
    ///
    /// @param key Key associated with the resource value to retrieve.
    /// @return    Resource bundle value or an empty string if not found.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(String)
    public String getValue(LocalizationKey key) {
        return getValue(key.getKey());
    }

    /// Returns all contained resource bundles.
    ///
    /// @return Immutable snapshot of all resource bundles at the time of calling.
    public Collection<ResourceBundle> getResourceBundles() {
        return providerStore.stream()
                      .map(ProviderEntry::getBundle)
                      .filter(Objects::nonNull)
                      .toList();
    }

    /// Formats the request as-is into a localized value.
    ///
    /// This method does **not** perform any post-processing on the given request.
    /// For example, argument resolution is not performed here, which [LocalizationValueBuilder] handles implicitly.
    ///
    /// This method is primarily useful for circumventing the builder API and implicit argument resolution.
    /// For all other use cases, prefer [get(String)] and [format(String)].
    ///
    /// @param request Request to format.
    /// @return        Formatted localized value.
    /// @throws NullPointerException If `request` is `null`.
    /// @see format(String)
    /// @see get(String)
    /// @since 2.0
    public String formatValue(LocalizationRequest request) {
        LocalizationRequestSource localizationRequestSource = request.getSource();
        String value = null;

        // In Java 21, this will be replaced with a switch
        if (localizationRequestSource instanceof LocalizationRequestSource.Key key) {
            value = formatValue(key, request);
        } else if (localizationRequestSource instanceof LocalizationRequestSource.Pattern pattern) {
            value = formatValue(pattern, request);
        }

        return value;
    }

    private String formatValue(LocalizationRequestSource.Pattern source, LocalizationRequest request) {
        // Since a pattern is given, `defaultValue` is not used here as a value will always be present
        return getFormatter().format(
            LocalizationFormatter.Request.Builder
                .of(source.value())
                .arguments(request.getArguments())
                .locale(getLocale())
                .build()
        );
    }

    private String formatValue(LocalizationRequestSource.Key source, LocalizationRequest request) {
        ResourceBundle bundle;
        String key = source.value();
        String value = null;

        LocalizationFormatter.Request.Builder requestBuilder = LocalizationFormatter.Request.Builder
                .of("")
                .arguments(request.getArguments());

        for (ProviderEntry entry : providerStore) {
            if ((bundle = entry.getBundle()) == null || !bundle.containsKey(key)) continue;

            String pattern = bundle.getString(key);

            try {
                value = getFormatter().format(
                    requestBuilder
                        .pattern(pattern)
                        // Locale can change mid-loop, so it's always set here
                        .locale(getLocale())
                        .build()
                );
            } catch (RuntimeException e) {
                if (!getConfig().isIgnoreProcessingExceptions()) {
                    throw e;
                }
            }

            if (value != null) {
                break;
            }
        }

        if (value == null) {
            if (request.hasDefaultValue()) {
                value = request.getDefaultValue();
            } else if (getConfig().isThrowWhenNoValueFound()) {
                throw new MissingResourceException(
                    "Cannot find resource for " + getClass().getName() +
                            ", key " + key +
                            ", bundles: " + getResourceBundles(),
                    getClass().getName(),
                    key
                );
            } else {
                value = getConfig().getDefaultMissingValue();
            }
        }

        return value;
    }

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances with a given [Locale].
    ///
    /// @param locale Locale to refresh all providers with.
    protected void refresh(Locale locale) {
        record VersionBundle(long version, ResourceBundle bundle) {}

        Map<ProviderEntry, VersionBundle> newBundles = new IdentityHashMap<>();

        for (ProviderEntry entry : providerStore) {
            // Stop early if the locale changes mid-way or the thread is interrupted
            if (!locale.equals(getLocale()) || Thread.currentThread().isInterrupted()) return;

            long version = entry.version.incrementAndGet();
            ResourceBundle bundle = getResourceBundle(entry, locale);
            newBundles.put(entry, new VersionBundle(version, bundle));
        }

        // Apply the new bundles
        synchronized (providerLock) {
            if (locale.equals(getLocale())) {
                for (Map.Entry<ProviderEntry, VersionBundle> mapEntry : newBundles.entrySet()) {
                    ProviderEntry entry = mapEntry.getKey();
                    VersionBundle versionBundle = mapEntry.getValue();

                    if (entry.version.get() == versionBundle.version) {
                        entry.bundle = versionBundle.bundle;
                    }
                }
            }
        }
    }

    private void refresh(ProviderEntry entry) {
        long version = entry.version.incrementAndGet();
        ResourceBundle bundle = getResourceBundle(entry, getLocale());

        synchronized (providerLock) {
            if (version == entry.version.get()) {
                entry.bundle = bundle;
            }
        }
    }

    /// @return The corresponding [ResourceBundle], or `null` if it was not found
    ///         and [LocalizeConfig#isIgnoreMissingResourceBundles()] is `true`.
    private ResourceBundle getResourceBundle(ProviderEntry entry, Locale locale) {
        ResourceBundle bundle = null;

        try {
            bundle = entry.getProvider().getBundle(locale);
        } catch (MissingResourceException e) {
            if (!getConfig().isIgnoreMissingResourceBundles()) {
                throw e;
            }
        }
        return bundle;
    }

    private static Locale assertLocale(Locale locale) {
        return Objects.requireNonNull(locale, "locale must not be null");
    }

    private static final class LocalizeImpl extends Localize {
        private final AtomicReference<Locale> locale;

        private LocalizeImpl(Locale locale, LocalizeConfig config) {
            super(config);
            this.locale = new AtomicReference<>(locale);
        }

        @Override public void setLocale(Locale locale) {
            assertLocale(locale);
            // If the given new `locale` is equivalent to the current locale,
            // no replacement is performed, matching `LocalizeFXImpl#setLocale`.
            Locale previous = this.locale.getAndUpdate(old -> old.equals(locale) ? old : locale);

            if (!locale.equals(previous)) {
                refresh(locale);
            }
        }

        @Override public Locale getLocale() {
            return locale.get();
        }
    }

    /// Container to encapsulate a resource bundle and associated fields.
    private static final class ProviderEntry {
        private final String key;
        private final ResourceBundleProvider provider;
        /// A counter to stop stale refreshes early.
        private final AtomicLong version = new AtomicLong();
        private volatile ResourceBundle bundle;

        /// Creates an entry container instance.
        ///
        /// @param key      Identifier of this entry instance to construct.
        /// @param provider Provider to fetch new resource bundles on refresh.
        private ProviderEntry(String key, ResourceBundleProvider provider) {
            this.key = Objects.requireNonNull(key, "key must not be null");
            this.provider = Objects.requireNonNull(provider, "provider must not be null");
        }

        /// {@return The entry identifier}
        public String getKey() {
            return key;
        }

        /// {@return Provider instance}
        public ResourceBundleProvider getProvider() {
            return provider;
        }

        /// May be `null` if [LocalizeConfig#isIgnoreMissingResourceBundles()]
        /// is set to `true` and the most recent fetch failed.
        ///
        /// @return Fetched resource bundle.
        public ResourceBundle getBundle() {
            return bundle;
        }
    }

    // Uses a list instead of Map as the number of providers is typically small (1~15).
    // Reads/iteration are **far greater** than writes
    private static final class ProviderStore extends CopyOnWriteArrayList<ProviderEntry> {
        public ProviderEntry get(String key) {
            for (ProviderEntry entry : this) {
                if (entry.getKey().equals(key)) {
                    return entry;
                }
            }
            return null;
        }

        // Synchronized to ensure that no modifications occur during iteration
        // (e.g., if `remove` is called, then it'll wait until this method completes)
        public synchronized boolean put(ProviderEntry newEntry) {
            for (int i = 0; i < size(); i++) {
                ProviderEntry entry = get(i);

                if (entry.getKey().equals(newEntry.getKey())) {
                    set(i, newEntry);
                    return false;
                }
            }
            return add(newEntry);
        }

        public synchronized boolean remove(String key) {
            return removeIf(entry -> entry.getKey().equals(key));
        }
    }
}
