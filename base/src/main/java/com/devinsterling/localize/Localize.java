package com.devinsterling.localize;

import com.ibm.icu.text.MessageFormat;

import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
/// The default [LocalizationRequestProcessor], [#DEFAULT_PROCESSOR],
/// includes support for named arguments, pluralization, and many other aspects based on
/// [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/#platform-dependencies).
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
/// MyApp.people = There {num_people, plural,\
///   =0{are no people on {location}.}\
///   =1{is one person on {location}.}\
///   other{are # people on {location}.}}
/// ```
///
/// The following localization files may be called as such:
/// ```java
/// Localize localize = new Localize();
/// localize.putBundleProvider("key1", locale -> {
///    return ResourceBundle.getBundle("sample.message", locale);
/// });
///
/// // We may now change the locale a number of times.
/// localize.setLocale(Locale.CHINESE);
/// assert(localize.getValue("MyApp.greet").equals("早上好"));
///
/// localize.setLocale(Locale.JAPAN);
/// assert(localize.getValue("MyApp.greet").equals("おはよう"));
///
/// localize.setLocale(Locale.ENGLISH);
/// assert(localize.get("MyApp.say1")
///                .arg("Hi")
///                .arg("Devin")
///                .value()
///                .equals("Hi, Devin"));
///
/// assert(localize.get("MyApp.say2")
///                .arg("name", "Devin")
///                .arg("intro", "Hi")
///                .value()
///                .equals("Hi, Devin"));
///
/// assert(localize.get("MyApp.people")
///                .arg("location", "campus")
///                .arg("num_people", 100)
///                .value()
///                .equals("There are 100 people on campus."));
/// ```
/// @since 1.0
public abstract class Localize {
    /// The default processor to handle converting a [LocalizationRequest]
    /// into a formatted localized string.
    public static final LocalizationRequestProcessor DEFAULT_PROCESSOR = Localize::processRequest;
    private final ProviderStore providerStore = new ProviderStore();
    private final LocalizeConfig config;
    private volatile LocalizationRequestProcessor processor = DEFAULT_PROCESSOR;

    /// Creates a [Localize] instance with the desired configuration.
    ///
    /// @param config The configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected Localize(LocalizeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.config = config;
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
        Objects.requireNonNull(locale, "locale must not be null");
        return new LocalizeImpl(locale, config);
    }

    /// Sets the request processor.
    ///
    /// The processor is called each time a request is made to fetch a value.
    ///
    /// @param processor Processor to handle requests.
    public void setProcessor(LocalizationRequestProcessor processor) {
        Objects.requireNonNull(processor, "Processor must not be null");
        this.processor = processor;
    }

    /// {@return The request processor}
    public LocalizationRequestProcessor getProcessor() {
        return processor;
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
    public synchronized boolean putBundleProvider(String key, ResourceBundleProvider provider) {
        ProviderEntry entry = new ProviderEntry(key, provider);
        refreshResourceBundle(entry, getLocale());
        return providerStore.put(entry);
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
    /// @since 1.3
    public synchronized String addBundleProvider(ResourceBundleProvider provider) {
        String uniqueKey;
        // Ensure the key is unique.
        // NOTE: In nearly every single case there is only 1 iteration
        do {
            uniqueKey = UUID.randomUUID().toString();
        } while (providerStore.get(uniqueKey) != null);

        putBundleProvider(uniqueKey, provider);
        return uniqueKey;
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

        if (entry != null) {
            refreshResourceBundle(entry, getLocale());
        }
        return entry != null;
    }

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances.
    ///
    /// Useful for reloading bundles from external sources (e.g., disk)
    /// after their contents have changed during runtime.
    public void refresh() {
        refresh(getLocale());
    }

    /// Returns a builder instance to get a formatted localized string.
    ///
    /// @param key Key associated with the resource value to retrieve.
    /// @return    **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `key` is `null`.
    /// @see LocalizationValueBuilder#value
    public LocalizationValueBuilder<?> get(String key) {
        return new LocalizationValueBuilder<>(key, this::applyBuilderProperties);
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

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances with a given [Locale].
    ///
    /// @param locale Locale to refresh all providers with.
    protected void refresh(Locale locale) {
        for (ProviderEntry entry : providerStore) {
            refreshResourceBundle(entry, locale);
        }
    }

    /// Applies and transforms the request into a formatted localized string.
    ///
    /// @param request Request to format string with.
    /// @return Requested formatted localized string.
    protected String applyBuilderProperties(LocalizationRequest request) {
        String value = null;

        for (ProviderEntry entry : providerStore) {
            if (entry.getBundle() != null) try {
                value = getProcessor().process(entry.getBundle(), request);

                if (value != null) {
                    break;
                }
            } catch (RuntimeException e) {
                if (!getConfig().isIgnoreProcessingExceptions()) {
                    throw e;
                }
            }
        }

        if (value == null) {
            if (request.hasDefaultValue()) {
                value = request.getDefaultValue();
            } else if (getConfig().isThrowWhenNoValueFound()) {
                throw new MissingResourceException(
                        "Cannot find resource for " + getClass().getName() +
                                ", key " + request.getKey() +
                                ", bundles: " + getResourceBundles(),
                        getClass().getName(),
                        request.getKey()
                );
            } else {
                value = getConfig().getDefaultMissingValue();
            }
        }
        return value;
    }

    private void refreshResourceBundle(ProviderEntry entry, Locale locale) {
        try {
            entry.refresh(locale);
        } catch (MissingResourceException e) {
            if (!getConfig().isIgnoreMissingResourceBundles()) {
                throw e;
            }
        }
    }

    private static String processRequest(ResourceBundle bundle, LocalizationRequest request) {
        String value = null;

        if (bundle.containsKey(request.getKey())) {
            value = bundle.getString(request.getKey());

            if (request.hasArguments()) {
                value = MessageFormat.format(value, request.getArguments());
            }
        }

        return value;
    }

    private static final class LocalizeImpl extends Localize {
        private volatile Locale locale;

        private LocalizeImpl(Locale locale, LocalizeConfig config) {
            super(config);
            this.locale = locale;
        }

        @Override public synchronized void setLocale(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null");

            if (!this.locale.equals(locale)) {
                this.locale = locale;
                refresh(locale);
            }
        }

        @Override public Locale getLocale() {
            return locale;
        }
    }

    /// Container to encapsulate a resource bundle and associated fields.
    private static final class ProviderEntry {
        private final String key;
        private final ResourceBundleProvider provider;
        private volatile ResourceBundle bundle;

        /// Creates an entry container instance.
        ///
        /// @param key      Identifier of this entry instance to construct.
        /// @param provider Provider to fetch new resource bundles on refresh.
        public ProviderEntry(String key, ResourceBundleProvider provider) {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(provider, "provider must not be null");

            this.key = key;
            this.provider = provider;
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

        /// Fetches a new resource bundle using this entry's [ResourceBundleProvider].
        ///
        /// @param locale Locale associated with the resource bundle.
        /// @see #getProvider()
        public void refresh(Locale locale) {
            this.bundle = getProvider().getBundle(locale);
        }
    }

    // Uses a list instead of Map as the number of providers is typically small (1~15).
    // Reads/iteration are **far greater** than writes
    private static class ProviderStore extends CopyOnWriteArrayList<ProviderEntry> {
        public ProviderEntry get(String key) {
            for (ProviderEntry entry : this) {
                if (entry.getKey().equals(key)) {
                    return entry;
                }
            }
            return null;
        }

        // Synchronized to ensure that no modifications occur during iteration.
        public synchronized boolean put(ProviderEntry entry) {
            for (int i = 0; i < size(); i++) {
                if (get(i).getKey().equals(entry.getKey())) {
                    set(i, entry);
                    return false;
                }
            }
            return add(entry);
        }

        public boolean remove(String key) {
            return removeIf(entry -> entry.getKey().equals(key));
        }
    }
}
