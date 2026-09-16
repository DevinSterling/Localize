package com.devinsterling.localize;

import com.devinsterling.localize.event.EventListener;
import com.devinsterling.localize.event.LocaleChangeEvent;
import com.devinsterling.localize.event.LocalizeEvent;
import com.devinsterling.localize.event.ProviderChangeEvent;
import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.event.impl.EventListenerRegistry;
import com.devinsterling.localize.event.impl.ProviderChangeEventImpls;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/// Base class to handle localization.
///
/// It is recommended to create a thread-safe [Localize]
/// instance through the static factory methods listed here:
/// - [of()]
/// - [of(Locale)]
/// - [of(LocalizeConfig)]
/// - [of(Locale, LocalizeConfig)]
///
/// All [Localize] methods are thread-safe unless specified otherwise by an implementation
/// (e.g., `LocalizeFX`, `LocalizeSwing`).
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
/// #### Example
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
/// localize.addProvider("sample.message");
/// // Or alternatively for more control
/// localize.addProvider(locale -> ResourceBundle.getBundle("sample.message", locale));
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
///
/// ### Events
/// Event handling is primarily managed using an [EventListener],
/// which listens to events fired by [Localize] instances (e.g., [LocaleChangeEvent], [ProviderChangeEvent]).
///
/// Listeners are added by calling [addListener], which stores listeners by **strong** reference.
/// To prevent memory leaks when no longer needed, listeners are removed using [removeListener]
/// or by disposing the [Subscription] returned by [addListener]:
/// ```java
/// Localize localize = Localize.of(Locale.JAPANESE);
/// // Creating a listener
/// EventListener<LocaleChangeEvent> listener = event -> {
///     logger.info("New locale = {}", event.getNewLocale());
/// };
///
/// // Registering a listener
/// Subscription subscription = localize.addListener(LocaleChangeEvent.class, listener);
///
/// // Deregistering a listener
/// subscription.dispose();
/// // or
/// localize.removeListener(LocaleChangeEvent.class, listener);
/// ```
/// Subclasses can override protected hooks to handle events
/// without registering listeners or managing their lifecycle:
/// ```
/// class Custom extends Localize {
///     ...
///     @Override protected void onLocaleChanged(LocaleChangeEvent event) {
///         super.onLocaleChanged(event);
///         ...
///     }
/// }
/// ```
///
/// ### Attaching and Shared State
/// [Localize] instances can be attached to adapt to specific environments while sharing the same underlying state.
///
/// Attached instances share the same [locale][getLocale], [formatter][getFormatter],
/// [providers][getProviderEntries], [configuration][getConfig], [listeners][addListener],
/// and are [notified of events made by either][fireEvent].
/// Associated changes are reflected in all other instances sharing the same state.
///
/// **Attaching does not use the source as a delegate. Method calls will not be routed through it.**
/// Aside from the shared internal state, instances are independent:
/// ```java
/// Localize localize = Localize.of(Locale.ENGLISH);
/// LocalizeFX javaFX = LocalizeFX.attach(localize); // JavaFX integration: localize-fx
/// LocalizeSwing swing = LocalizeSwing.attach(localize); // Swing integration: localize-swing
///
/// // Changes made to one are reflected to all instances:
/// javaFX.setLocale(Locale.JAPANESE);
/// assert swing.getLocale().equals(Locale.JAPANESE); // true
///
/// swing.setLocale(Locale.KOREAN);
/// assert localize.getLocale().equals(Locale.KOREAN); // true
/// ```
///
/// Independent instances can also be created directly when shared state is not needed:
/// ```java
/// Localize localize = Localize.of(Locale.ENGLISH);
/// LocalizeFX javaFX = LocalizeFX.of(Locale.ENGLISH);
/// LocalizeSwing swing = LocalizeSwing.of(Locale.ENGLISH);
/// ```
/// @since 1.0
public class Localize {
    private static final class Data {
        /// All [Localize] instances attached to this [Data].
        /// This avoids having to go through the [listeners] registry,
        /// allowing to notify attached instances without indirection.
        private final CopyOnWriteArrayList<WeakReference<Localize>> attached = new CopyOnWriteArrayList<>();
        /// Lock to synchronize [ProviderEntry#bundle] replacements
        private final Object providerEntryBundleLock = new Object();
        private final EventListenerRegistry listeners = new EventListenerRegistry();
        private final ProviderStore providers = new ProviderStore();
        private final VersionedLocale locale;
        private final LocalizeConfig config;
        private volatile LocalizationFormatter formatter = LocalizationFormatterLocator.PROVIDER.provide();

        private Data(Locale locale, LocalizeConfig config) {
            this.locale = new VersionedLocale(locale);
            this.config = Objects.requireNonNull(config, "config must not be null");
        }
    }

    private final Data data;
    /// Keeps the wrapped instance from being garbage collected.
    /// Without this, it can lead to subtle bugs:
    /// ```
    /// // LocalizeLogger can be GC'd too early if the wrapper doesn't keep a reference.
    /// Localize a = LocalizeFX.attach(
    ///     LocalizeLogger.attach(
    ///         Localize.of()
    ///     )
    /// );
    /// ```
    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private final Localize parent;

    private Localize(Localize parent, Data data) {
        this.parent = parent;
        this.data = data;
        data.attached.add(new WeakReference<>(this));
    }

    /// Creates a [Localize] instance attached to the same internal state as the given source,
    /// sharing the same [locale][getLocale], [formatter][getFormatter], [providers][getProviderEntries],
    /// [configuration][getConfig], [listeners][addListener], and is [notified of events made by either][fireEvent].
    ///
    /// **The given source is not used as a delegate. Method calls will not be routed through it.**
    /// Aside from the shared internal state, both instances are independent.
    ///
    /// @param source Instance with the internal state to attach to.
    /// @throws NullPointerException If `source` is `null`.
    /// @since 2.0
    protected Localize(Localize source) {
        this(Objects.requireNonNull(source, "source must not be null"), source.data);
    }

    /// Creates a [Localize] instance with the given locale and configuration.
    ///
    /// @param locale Initial locale.
    /// @param config Main localize configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected Localize(Locale locale, LocalizeConfig config) {
        this(null, new Data(locale, config));
    }

    /// Creates a new [Localize] instance with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** Localize instance.
    public static Localize of() {
        return of(Locale.getDefault());
    }

    /// Creates a new [Localize] instance with the given [Locale] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** Localize instance.
    /// @throws NullPointerException If `locale` is `null`.
    public static Localize of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Creates a new [Localize] instance with the given [LocalizeConfig]
    /// and initial locale set as [Locale#getDefault].
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
        return new Localize(locale, config);
    }

    /// A hook triggered whenever an event occurs.
    ///
    /// This method is called internally and should not be called directly by programs.
    ///
    /// When overriding, subclasses *should* call `super.onEvent` to preserve parent behavior:
    /// ```java
    /// @Override protected void onEvent(LocalizeEvent event) {
    ///     super.onEvent(event);
    ///     ...
    /// }
    /// ```
    /// @param event Localize event.
    /// @implSpec This method must be thread-safe.
    /// @see fireEvent
    /// @since 2.0
    protected void onEvent(LocalizeEvent event) {
        // In Java 21, this will be replaced with a switch
        if (event instanceof LocaleChangeEvent change) {
            onLocaleChanged(change);
        } else if (event instanceof ProviderChangeEvent change) {
            onProvidersChanged(change);
        }
    }

    /// A hook triggered whenever the locale is changed.
    ///
    /// This method is called internally and should not be called directly by programs.
    ///
    /// When overriding, subclasses *should* call `super.onLocaleChanged` to preserve parent behavior:
    /// ```java
    /// @Override protected void onLocaleChanged(LocaleChanged change) {
    ///     super.onLocaleChanged(change);
    ///
    ///     // Checking if the change is still fresh
    ///     if (change.isValid()) {
    ///         ...
    ///     }
    /// }
    /// ```
    /// @param change Locale change event.
    /// @implSpec This method must be thread-safe.
    /// @since 2.0
    protected void onLocaleChanged(LocaleChangeEvent change) {
        // no-op
    }

    /// A hook triggered whenever a provider is added, removed, or refreshed.
    ///
    /// This method is called internally and should not be called directly by programs.
    ///
    /// When overriding, subclasses *should* call `super.onProvidersChanged` to preserve parent behavior:
    /// ```java
    /// @Override protected void onProvidersChanged(ProviderChangeEvent event) {
    ///     super.onProvidersChanged(event);
    ///
    ///     // Inspecting the event
    ///     switch (event) {
    ///         case ProviderChangeEvent.Added added -> {
    ///             logger.info("Added provider {}", added.getEntry().getKey());
    ///         }
    ///         case ProviderChangeEvent.Refreshed refreshed -> {
    ///             // ...
    ///         }
    ///         default -> {}
    ///     }
    /// }
    /// ```
    /// @param event Provider change event.
    /// @implSpec This method must be thread-safe.
    /// @since 2.0
    protected void onProvidersChanged(ProviderChangeEvent event) {
        // no-op
    }

    /// Adds the given listener, notifying it whenever the specified event occurs.
    ///
    /// If the given listener is already registered, it is not re-inserted and
    /// a reference to its existing associated [Subscription] is returned.
    ///
    /// ### Memory
    /// The given listener is stored by a strong reference.
    /// To prevent memory leaks when the listener is no longer needed,
    /// call [removeListener] or [Subscription#dispose]:
    /// ```java
    /// // Creating a listener
    /// EventListener<LocaleChangeEvent> listener = event -> {
    ///     logger.info("New locale set: {}", event.getNewLocale());
    /// };
    ///
    /// // Registering a listener
    /// Subscription subscription = localize.addListener(LocaleChangeEvent.class, listener);
    ///
    /// // Deregistering a listener
    /// subscription.dispose();
    /// // or
    /// localize.removeListener(LocaleChangeEvent.class, listener);
    /// ```
    /// @param <T>       Event type.
    /// @param eventType Type of event to listen to.
    /// @param listener  Listener to add.
    /// @return         **Thread-safe** subscription to remove the added listener.
    /// @throws NullPointerException If `type` or `listener` is `null`.
    /// @since 2.0
    public <T extends LocalizeEvent> Subscription addListener(Class<T> eventType, EventListener<? super T> listener) {
        return data.listeners.addListener(eventType, listener);
    }

    /// Removes the given listener for the specified event, if it was [previously added][addListener].
    ///
    /// @param <T>       Event type.
    /// @param eventType ype of event to listen to.
    /// @param listener  Listener to remove.
    /// @return         `true` if the given listener was removed, or `false` if it isn't registered.
    /// @throws NullPointerException If `type` or `listener` is `null`.
    /// @since 2.0
    public <T extends LocalizeEvent> boolean removeListener(Class<T> eventType, EventListener<? super T> listener) {
        return data.listeners.removeListener(eventType, listener);
    }

    /// Sets the locale and updates all resource bundles.
    ///
    /// Changing the locale will trigger a [refresh][refreshProviders()].
    ///
    /// @param locale Locale to fetch associated resource bundles.
    /// @throws NullPointerException If locale is `null`.
    public void setLocale(Locale locale) {
        record Change(Localize localize, VersionedLocale.Snapshot snapshot) implements LocaleChangeEvent {
            @Override public Locale getOldLocale() {
                return snapshot.previous;
            }

            @Override public Locale getNewLocale() {
                return snapshot.current;
            }

            @Override public boolean isValid() {
                return localize.data.locale.isCurrent(snapshot);
            }

            @Override public Localize getSource() {
                return localize;
            }
        }

        VersionedLocale.Snapshot snapshot = this.data.locale.set(locale);

        if (!snapshot.isUnchanged()) {
            refreshProviders(snapshot, LocalizeEvent.Cause.LOCALE_CHANGE);
            fireEvent(new Change(this, snapshot));
        }
    }

    /// The current locale.
    ///
    /// @return The current locale.
    public Locale getLocale() {
        return data.locale.get();
    }

    /// Sets the localization formatter.
    ///
    /// The formatter is called each time a request is made to format a value.
    ///
    /// @param formatter Formatter to format requests.
    /// @throws NullPointerException If `formatter` is `null`.
    public void setFormatter(LocalizationFormatter formatter) {
        data.formatter = Objects.requireNonNull(formatter, "Formatter must not be null");
    }

    /// {@return The localization formatter}
    public LocalizationFormatter getFormatter() {
        return data.formatter;
    }

    /// {@return The localize configuration}
    public LocalizeConfig getConfig() {
        return data.config;
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
    /// Localize.ProviderKey key = Localize.ProviderKey.of("myKey");
    /// Localize.ProviderKey key2 = Localize.ProviderKey.of("myKey2");
    ///
    /// localize.putProvider(key, locale -> {
    ///     return ResourceBundle.getBundle("i18n.messages", locale);
    /// });
    ///
    /// // Has lower priority than the provider put first
    /// // (i.e., Localized value requests)
    /// localize.putProvider(key2, locale -> {
    ///     return ResourceBundle.getBundle("i18n.other", locale);
    /// });
    ///
    /// // Removal
    /// localize.removeProvider(key);
    /// ```
    ///
    /// @param key      Key associated with `provider`.
    /// @param provider Provider called upon calling refresh to get a ResourceBundle instance.
    /// @return         Previous provider associated with `key`, or `null` if there was none.
    /// @throws NullPointerException If `key` or `provider` is `null`.
    /// @see putProvider(String, ResourceBundleProvider)
    /// @see putProvider(ProviderKey, String)
    /// @see addProvider(ResourceBundleProvider)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public ResourceBundleProvider putProvider(ProviderKey key, ResourceBundleProvider provider) {
        ProviderEntry entry = new ProviderEntry(this, key, provider);
        ProviderEntry previous = data.providers.put(entry);
        refreshProvider(entry);

        fireEvent(
            previous == null
                ? new ProviderChangeEventImpls.Added(this, entry)
                : new ProviderChangeEventImpls.Replaced(this, previous, entry)
        );

        return previous != null ? previous.getProvider() : null;
    }

    /// Adds the given provider to retrieve localized values from.
    ///
    /// This is a convenience method, equivalent to calling
    /// [putProvider(ProviderKey, ResourceBundleProvider)]
    /// with [ProviderKey#of(String)].
    ///
    /// ### Example Usage
    /// ```java
    /// localize.putProvider("myKey", locale -> {
    ///     return ResourceBundle.getBundle("i18n.messages", locale);
    /// });
    ///
    /// // Has lower priority than the provider put first
    /// // (i.e., Localized value requests)
    /// localize.putProvider("myKey2", locale -> {
    ///     return ResourceBundle.getBundle("i18n.other", locale);
    /// });
    ///
    /// // Removal
    /// localize.removeProvider("myKey");
    /// ```
    /// @param key      Key associated with `provider`.
    /// @param provider Provider called upon calling refresh to get a ResourceBundle instance.
    /// @return         Previous provider associated with `key`, or `null` if there was none.
    /// @throws NullPointerException If `key` or `provider` is `null`.
    /// @see putProvider(String, String)
    public ResourceBundleProvider putProvider(String key, ResourceBundleProvider provider) {
        return putProvider(ProviderKey.of(key), provider);
    }

    /// Shorthand for [putProvider(ProviderKey, ResourceBundleProvider)]
    /// using [ResourceBundle#getBundle(String)].
    ///
    /// This is a convenience method, equivalent to calling:
    /// ```
    /// putProvider(key, locale -> ResourceBundle.getBundle(baseName, locale));
    /// ```
    /// @param key                    Key associated with `provider`.
    /// @param resourceBundleBaseName The base name of a [ResourceBundle].
    /// @return                       Previous provider associated with `key`, or `null` if there was none.
    /// @throws NullPointerException If `key` or `resourceBundleBaseName` is `null`.
    /// @see putProvider(String, ResourceBundleProvider)
    /// @see addProvider(String)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public ResourceBundleProvider putProvider(ProviderKey key, String resourceBundleBaseName) {
        return putProvider(key, locale -> ResourceBundle.getBundle(resourceBundleBaseName, locale));
    }

    /// Shorthand for [putProvider(String, ResourceBundleProvider)] using [ResourceBundle#getBundle(String)].
    ///
    /// This is a convenience method, equivalent to calling [putProvider(ProviderKey, String)]
    /// with [ProviderKey#of(String)].
    ///
    /// @param key                    Key associated with the provider.
    /// @param resourceBundleBaseName The base name of a [ResourceBundle].
    /// @return                       Previous provider associated with `key`, or `null` if there was none.
    /// @throws NullPointerException If `key` or `resourceBundleBaseName` is `null`.
    /// @see putProvider(String, ResourceBundleProvider)
    /// @since 2.0
    public ResourceBundleProvider putProvider(String key, String resourceBundleBaseName) {
        return putProvider(ProviderKey.of(key), resourceBundleBaseName);
    }

    /// Adds the given provider and returns the associated provider entry.
    ///
    /// This method is equivalent to [putProvider(ProviderKey, ResourceBundleProvider)]
    /// without the need to manually specify a key.
    ///
    /// @param provider Provider called upon calling refresh to get a ResourceBundle instance.
    /// @return         The provider entry, if needed.
    /// @throws NullPointerException If `provider` is `null`.
    /// @see addProvider(String)
    /// @see putProvider(ProviderKey, ResourceBundleProvider)
    /// @since 1.3
    public ProviderEntry addProvider(ResourceBundleProvider provider) {
        ProviderEntry entry = new ProviderEntry(this, ProviderKey.of(), provider);
        data.providers.put(entry);
        refreshProvider(entry);
        fireEvent(new ProviderChangeEventImpls.Added(this, entry));
        return entry;
    }

    /// Shorthand for [addProvider(ResourceBundleProvider)] using [ResourceBundle#getBundle(String)].
    ///
    /// This is a convenience method, equivalent to calling:
    /// ```
    /// addProvider(locale -> ResourceBundle.getBundle(baseName, locale));
    /// ```
    /// @param resourceBundleBaseName The base name of a [ResourceBundle].
    /// @return                       The provider entry, if needed.
    /// @throws NullPointerException If `resourceBundleBaseName` is `null`.
    /// @see addProvider(ResourceBundleProvider)
    /// @see putProvider(ProviderKey, String)
    /// @since 2.0
    public ProviderEntry addProvider(String resourceBundleBaseName) {
        return addProvider(locale -> ResourceBundle.getBundle(resourceBundleBaseName, locale));
    }

    /// Returns the entry associated with the given key, or `null` if not present.
    ///
    /// @param key Key of the provider to retrieve the entry for.
    /// @return    Provider entry or `null` if not present.
    /// @throws NullPointerException If `key` is `null`.
    /// @see getProviderEntry(String)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public ProviderEntry getProviderEntry(ProviderKey key) {
        return data.providers.get(Objects.requireNonNull(key, "key must not be null"));
    }

    /// Returns the entry associated with the given key, or `null` if not present.
    ///
    /// This is a convenience method, equivalent to calling [getProviderEntry(ProviderKey)]
    /// with [ProviderKey#of(String)].
    ///
    /// @param key Key of the provider to retrieve the entry for.
    /// @return    Provider entry or `null` if not present.
    /// @throws NullPointerException If `key` is `null`.
    /// @since 2.0
    public ProviderEntry getProviderEntry(String key) {
        return getProviderEntry(ProviderKey.of(key));
    }

    /// Returns `true` if the provider associated with the given key is present.
    ///
    /// @param key Key to check if the associated provider is present.
    /// @return    `true` if the associated provider is present.
    /// @throws NullPointerException If `key` is `null`.
    /// @see containsProvider(String)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public boolean containsProvider(ProviderKey key) {
        return data.providers.contains(Objects.requireNonNull(key, "key must not be null"));
    }

    /// Returns `true` if the provider associated with the given key is present.
    ///
    /// This is a convenience method, equivalent to calling [containsProvider(ProviderKey)]
    /// with [ProviderKey#of(String)].
    ///
    /// @param key Key to check if the associated provider is present.
    /// @return    `true` if the associated provider is present.
    /// @see ProviderKey#of(String)
    /// @throws NullPointerException If `key` is `null`.
    /// @since 2.0
    public boolean containsProvider(String key) {
        return containsProvider(ProviderKey.of(key));
    }

    /// Removes the [ResourceBundleProvider] associated with the given key.
    ///
    /// @param key Key associated with the provider to remove.
    /// @return    The removed provider or `null` if the requested provider was not found.
    /// @throws NullPointerException if `key` is `null`.
    /// @see removeProvider(String)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public ResourceBundleProvider removeProvider(ProviderKey key) {
        Objects.requireNonNull(key, "key must not be null");
        ProviderEntry removed = data.providers.remove(key);

        if (removed != null) {
            fireEvent(new ProviderChangeEventImpls.Removed(this, removed));
        }

        return removed == null ? null : removed.getProvider();
    }

    /// Removes the [ResourceBundleProvider] associated with the given key.
    ///
    /// This is a convenience method, equivalent to calling [removeProvider(ProviderKey)]
    /// with [ProviderKey#of(String)].
    ///
    /// @param key Key associated with the provider to remove.
    /// @return    The removed provider or `null` if the requested provider was not found.
    /// @throws NullPointerException if `key` is `null`.
    public ResourceBundleProvider removeProvider(String key) {
        return removeProvider(ProviderKey.of(key));
    }

    /// Removes all provider entries and returns `true` if any were removed.
    ///
    /// @return `true` if any entries were removed, or `false` if there were no entries to remove.
    /// @since 2.0
    public boolean clearProviders() {
        List<ProviderEntry> removed = data.providers.clear();
        boolean isAnyRemoved = !removed.isEmpty();

        if (isAnyRemoved) {
            fireEvent(
                removed.size() > 1
                    ? new ProviderChangeEventImpls.BulkRemoved(this, removed)
                    // If one entry was removed, no need to fire a bulk refresh event
                    : new ProviderChangeEventImpls.Removed(this, removed.get(0))
            );
        }

        return isAnyRemoved;
    }

    /// Triggers a refresh by fetching a new [ResourceBundle] from
    /// the [ResourceBundleProvider] associated with the given key.
    ///
    /// Useful for reloading a specific bundle from an external source (e.g., disk)
    /// after its contents have changed during runtime.
    ///
    /// ### Concurrency
    /// If a more recent concurrent call to [setLocale] or `refresh` with the same key occurs
    /// during this method call, the newest call takes precedence and this method returns `false`.
    ///
    /// @param key Key associated with the provider to refresh.
    /// @return    `true` if the provider was refreshed.
    ///            Otherwise, `false` is returned if the provider was not found or superseded by a newer refresh.
    /// @throws NullPointerException If `key` is `null`.
    /// @see refreshProvider(String)
    /// @see putProvider(String, ResourceBundleProvider)
    /// @see ProviderKey#of(String)
    /// @since 2.0
    public boolean refreshProvider(ProviderKey key) {
        Objects.requireNonNull(key, "key must not be null");
        ProviderEntry entry = data.providers.get(key);
        boolean isRefreshed = entry != null && refreshProvider(entry);

        if (isRefreshed) {
            fireEvent(new ProviderChangeEventImpls.Refreshed(this, entry));
        }

        return isRefreshed;
    }

    /// Triggers a refresh by fetching a new [ResourceBundle] from
    /// the [ResourceBundleProvider] associated with the given key.
    ///
    /// This is a convenience method, equivalent to calling [refreshProvider(ProviderKey)]
    /// with [ProviderKey#of(String)].
    ///
    /// @param key Key associated with the provider to refresh.
    /// @return    `true` if the provider was refreshed.
    ///            Otherwise, `false` is returned if the provider was not found or superseded by a newer refresh.
    /// @throws NullPointerException If `key` is `null`.
    public boolean refreshProvider(String key) {
        return refreshProvider(ProviderKey.of(key));
    }

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances.
    /// Returns `true` if any providers were refreshed.
    ///
    /// Useful for reloading bundles from external sources (e.g., disk)
    /// after their contents have changed during runtime.
    ///
    /// @return `true` if any providers were refreshed, or `false` if none were refreshed.
    public boolean refreshProviders() {
        return refreshProviders(data.locale.snapshot(), LocalizeEvent.Cause.EXTERNAL);
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
    /// @param key Resource bundle key associated with the value to retrieve.
    /// @return    **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `key` is `null`.
    /// @see LocalizationValueBuilder#value
    /// @see format
    public LocalizationValueBuilder<?> get(String key) {
        return new LocalizationValueBuilder<>(new LocalizationRequestSource.Key(key), this);
    }

    /// Returns a builder for formatting a localized value retrieved from the given resource key.
    ///
    /// This method is equivalent to [get(String)].
    ///
    /// @param key Resource bundle key associated with the value to retrieve.
    /// @return    **Non-thread-safe** builder instance to format the requested value.
    /// @throws NullPointerException If `key` is `null`.
    /// @see LocalizationValueBuilder#value
    public LocalizationValueBuilder<?> get(LocalizationKey key) {
        return get(key.getKey());
    }

    /// Retrieves the value associated with the given resource bundle key.
    ///
    /// @param key Resource bundle key associated with the value to retrieve.
    /// @return    Resource bundle value or the [default value][LocalizeConfig#getDefaultMissingValue] if not found.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(LocalizationKey)
    public String getValue(String key) {
        return formatValue(LocalizationRequest.ofKey(key));
    }

    /// Retrieves the value associated with the given resource bundle key.
    ///
    /// This method is equivalent to [getValue(String)].
    ///
    /// @param key Resource bundle key associated with the value to retrieve.
    /// @return    Resource bundle value or the [default value][LocalizeConfig#getDefaultMissingValue] if not found.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(String)
    public String getValue(LocalizationKey key) {
        return getValue(key.getKey());
    }

    /// Returns an unmodifiable live view of all provider entries,
    /// ordered by their [priority][putProvider(ProviderKey, ResourceBundleProvider)].
    ///
    /// @return Unmodifiable live view of all entries.
    /// @since 2.0
    public Collection<ProviderEntry> getProviderEntries() {
        return data.providers.unmodifiableView;
    }

    /// Returns all contained resource bundles.
    ///
    /// @return Immutable snapshot of all resource bundles at the time of calling.
    public Collection<ResourceBundle> getResourceBundles() {
        return data.providers
                   .unmodifiableView
                   .stream()
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

        for (ProviderEntry entry : data.providers) {
            if ((bundle = entry.getBundle()) == null || !bundle.containsKey(key)) continue;

            String pattern = bundle.getString(key);

            try {
                value = getFormatter().format(
                    LocalizationFormatter.Request.Builder
                        .of(pattern)
                        .arguments(request.getArguments())
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

    /// Triggers all providers to refresh and fetch new [ResourceBundle] instances with a given [Locale],
    /// returning `true` if any providers were refreshed.
    ///
    /// @param snapshot Snapshot locale to refresh all providers with.
    /// @return       `true` if any providers were refreshed.
    private boolean refreshProviders(VersionedLocale.Snapshot snapshot, LocalizeEvent.Cause cause) {
        record VersionEntryBundle(ProviderEntry entry, ResourceBundle bundle, long version) {}

        int sizeHint = data.providers.providers.size();
        List<VersionEntryBundle> newBundles = new ArrayList<>(sizeHint);

        for (ProviderEntry entry : data.providers) {
            // Stop early if the locale changes mid-way or the thread is interrupted
            if (!data.locale.isCurrent(snapshot) || Thread.currentThread().isInterrupted()) {
                return false;
            }

            long version = entry.version.incrementAndGet();
            ResourceBundle bundle = getResourceBundle(entry, snapshot.current);
            newBundles.add(new VersionEntryBundle(entry, bundle, version));
        }

        List<ProviderEntry> refreshed = new ArrayList<>(sizeHint);

        // Apply the new bundles
        synchronized (data.providerEntryBundleLock) {
            if (data.locale.isCurrent(snapshot)) {
                for (VersionEntryBundle versionEntryBundle : newBundles) {
                    ProviderEntry entry = versionEntryBundle.entry;
                    ResourceBundle newBundle = versionEntryBundle.bundle;

                    if (// A refresh occurs if at least one of the bundles is non-null.
                        (entry.bundle != null || newBundle != null)
                        && entry.isActive()
                        && entry.version.get() == versionEntryBundle.version
                    ) {
                        entry.bundle = newBundle;
                        refreshed.add(entry);
                    }
                }
            }
        }

        if (!refreshed.isEmpty()) {
            fireEvent(
                refreshed.size() > 1
                    ? new ProviderChangeEventImpls.BulkRefreshed(this, cause, refreshed)
                    : new ProviderChangeEventImpls.Refreshed(this, cause, refreshed.get(0))
            );
        }

        return !refreshed.isEmpty();
    }

    private boolean refreshProvider(ProviderEntry entry) {
        if (!entry.isActive()) return false;

        boolean isRefreshed = false;
        long version = entry.version.incrementAndGet();
        VersionedLocale.Snapshot snapshot = data.locale.snapshot();
        ResourceBundle newBundle = getResourceBundle(entry, snapshot.current);

        synchronized (data.providerEntryBundleLock) {
            if (// A refresh occurs if at least one of the bundles is non-null.
                (entry.bundle != null || newBundle != null)
                && entry.isActive()
                && data.locale.isCurrent(snapshot)
                && version == entry.version.get()
            ) {
                entry.bundle = newBundle;
                isRefreshed = true;
            }
        }

        return isRefreshed;
    }

    private void removeProvider(ProviderEntry entry) {
        if (entry.isActive() && data.providers.remove(entry)) {
            fireEvent(new ProviderChangeEventImpls.Removed(this, entry));
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

    /// Fires the given event and propagates it to all attached [Localize] instances.
    ///
    /// The event is dispatched to the [onEvent] hook of each attached instance.
    ///
    /// @param event Event to fire and propagate.
    /// @throws NullPointerException If `event` is `null`.
    /// @since 2.0
    protected final void fireEvent(LocalizeEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        boolean needsCleanup = false;

        for (WeakReference<Localize> weak : data.attached) {
            Localize instance = weak.get();
            if (instance != null) {
                instance.onEvent(event);
            } else {
                needsCleanup = true;
            }
        }

        if (needsCleanup) {
            data.attached.removeIf(weak -> weak.get() == null);
        }

        data.listeners.fireEvent(event);
    }

    /// A unique key associated with a [ResourceBundleProvider] [entry][ProviderEntry].
    ///
    /// Keys are created using the static factory methods listed here:
    /// - [of()] to create a unique key.
    /// - [of(String)] to create a string key.
    ///
    /// @implSpec Implementations must ensure [Object#equals] and [Object#hashCode] are properly overridden, if needed.
    /// @see ProviderEntry
    /// @since 2.0
    public interface ProviderKey {
        /// Returns a new unique key that is only equal to itself.
        ///
        /// ### Example Usage
        /// Comparing equality:
        /// ```
        /// Localize.ProviderKey a = Localize.ProviderKey.of();
        /// Localize.ProviderKey b = Localize.ProviderKey.of();
        ///
        /// assert a.equals(b); // false
        /// ```
        /// @return New unique key.
        /// @see of(String)
        static ProviderKey of() {
            return new ProviderKey() {};
        }

        /// Returns a wrapper over a string key.
        ///
        /// ### Example Usage
        /// Comparing equality:
        /// ```
        /// Localize.ProviderKey a = Localize.ProviderKey.of("foo");
        /// Localize.ProviderKey b = Localize.ProviderKey.of("foo");
        /// Localize.ProviderKey c = Localize.ProviderKey.of("bar");
        ///
        /// assert a.equals(b); // true
        /// assert b.equals(c); // false
        /// ```
        /// @param key String key to wrap.
        /// @return    Wrapped string key.
        /// @throws NullPointerException If `key` is `null`.
        /// @see of()
        static ProviderKey of(String key) {
            record NamedKey(String key) implements ProviderKey {
                @Override public String toString() {
                    return key;
                }
            }

            return new NamedKey(Objects.requireNonNull(key, "key must not be null"));
        }
    }

    /// An entry associated with a specific [ResourceBundleProvider].
    ///
    /// Entries are containers that represent a provider registered within a [Localize] instance.
    ///
    /// @see Localize#getProviderEntry(ProviderKey)
    /// @see Localize#addProvider(ResourceBundleProvider)
    /// @since 2.0
    public static final class ProviderEntry {
        private final ProviderKey key;
        private final ResourceBundleProvider provider;
        /// A counter to stop stale refreshes early.
        private final AtomicLong version = new AtomicLong();
        private volatile ResourceBundle bundle;
        private volatile Localize localize;

        private ProviderEntry(Localize localize, ProviderKey key, ResourceBundleProvider provider) {
            this.localize = Objects.requireNonNull(localize, "localize must not be null");
            this.key = Objects.requireNonNull(key, "key must not be null");
            this.provider = Objects.requireNonNull(provider, "provider must not be null");
        }

        /// Returns the key associated with the contained [provider][getProvider].
        ///
        /// @return Provider key.
        public ProviderKey getKey() {
            return key;
        }

        /// Returns the contained provider.
        ///
        /// @return Contained provider.
        /// @see getKey
        public ResourceBundleProvider getProvider() {
            return provider;
        }

        /// Returns the most recently computed resource bundle.
        ///
        /// ### Note
        /// The returned bundle may be `null` if [LocalizeConfig#isIgnoreMissingResourceBundles()]
        /// is set to `true` and the most recent fetch failed, or if this entry is [inactive][isActive].
        ///
        /// @return Most recently computed resource bundle, or `null` if not available.
        public ResourceBundle getBundle() {
            return bundle;
        }

        /// Removes the contained provider from the associated [Localize] instance.
        ///
        /// This method has no effect if this entry is [inactive][isActive].
        ///
        /// @see Localize#removeProvider(ProviderKey)
        /// @see isActive
        public void remove() {
            Localize localize = this.localize;

            if (localize != null) {
                localize.removeProvider(this);
            }
        }

        /// Triggers a refresh on a [Localize] instance by fetching a new [ResourceBundle] from
        /// the [ResourceBundleProvider] associated with the given key.
        ///
        /// This method has no effect if this entry is [inactive][isActive].
        ///
        /// @see Localize#refreshProvider(ProviderKey)
        /// @see isActive
        public void refresh() {
            Localize localize = this.localize;

            if (localize != null && localize.refreshProvider(this)) {
                localize.fireEvent(new ProviderChangeEventImpls.Refreshed(localize, this));
            }
        }

        /// Returns `true` if this entry is currently active; registered within a [Localize] instance.
        ///
        /// When the associated provider is
        /// [replaced][Localize#putProvider(ProviderKey, ResourceBundleProvider)] or
        /// [removed][Localize#removeProvider(ProviderKey)]
        /// via any related operation, this flag will permanently remain `false`.
        ///
        /// @return `true` if active, or `false` if the contained provider was replaced or removed prior.
        /// @see Localize#containsProvider(ProviderKey)
        public boolean isActive() {
            return localize != null;
        }

        private void dispose() {
            version.incrementAndGet();
            localize = null;
            bundle = null;
        }
    }

    // Uses a list instead of Map as the number of providers is typically small (1~15).
    // Reads/iteration are **far greater** than writes
    private static final class ProviderStore implements Iterable<ProviderEntry> {
        CopyOnWriteArrayList<ProviderEntry> providers = new CopyOnWriteArrayList<>();
        Collection<ProviderEntry> unmodifiableView = Collections.unmodifiableCollection(providers);

        @Override public Iterator<ProviderEntry> iterator() {
            return providers.iterator();
        }

        private ProviderEntry get(ProviderKey key) {
            for (ProviderEntry entry : providers) {
                if (entry.getKey().equals(key)) {
                    return entry;
                }
            }

            return null;
        }

        private boolean contains(ProviderKey key) {
            return get(key) != null;
        }

        // Synchronized to ensure that no modifications occur during iteration
        // (e.g., if `remove` is called, then it'll wait until this method completes)
        private synchronized ProviderEntry put(ProviderEntry newEntry) {
            for (int i = 0; i < providers.size(); i++) {
                ProviderEntry entry = providers.get(i);

                if (entry.getKey().equals(newEntry.getKey())) {
                    providers.set(i, newEntry);
                    // When an entry is removed, it must be marked inactive by disposing it.
                    entry.dispose();
                    return entry;
                }
            }

            providers.add(newEntry);
            return null;
        }

        private synchronized ProviderEntry remove(ProviderKey key) {
            for (int i = 0; i < providers.size(); i++) {
                ProviderEntry entry = providers.get(i);

                if (entry.getKey().equals(key)) {
                    providers.remove(i);
                    // When an entry is removed, it must be marked inactive by disposing it.
                    entry.dispose();
                    return entry;
                }
            }

            return null;
        }

        private synchronized boolean remove(ProviderEntry entry) {
            boolean removed = providers.remove(entry);

            if (removed) {
                entry.dispose();
            }

            return removed;
        }

        /// @return Snapshot of the removed and disposed entries.
        private synchronized List<ProviderEntry> clear() {
            if (providers.isEmpty()) return List.of();

            List<ProviderEntry> removed = List.copyOf(providers);
            providers.clear();

            for (ProviderEntry entry : removed) {
                entry.dispose();
            }

            return removed;
        }
    }

    private static final class VersionedLocale {
        private volatile long version = 0;
        private volatile Locale locale;

        private VersionedLocale(Locale locale) {
            this.locale = assertLocale(locale);
        }

        private synchronized Snapshot set(Locale newLocale) {
            Locale current = locale;

            // If the given `newLocale` is equivalent to the current `locale`, no replacement is performed.
            if (current.equals(assertLocale(newLocale))) {
                // version is `-1` if the given locale is equivalent.
                return Snapshot.unchanged(current);
            } else {
                return new Snapshot(current, locale = newLocale, ++version);
            }
        }

        private synchronized Snapshot snapshot() {
            return new Snapshot(locale, locale, version);
        }

        private Locale get() {
            return locale;
        }

        private boolean isCurrent(Snapshot snapshot) {
            return snapshot.version == this.version;
        }

        private record Snapshot(Locale previous, Locale current, long version) {
            static Snapshot unchanged(Locale locale) {
                return new Snapshot(locale, locale, -1);
            }

            private boolean isUnchanged() {
                return version == -1;
            }
        }
    }
}
