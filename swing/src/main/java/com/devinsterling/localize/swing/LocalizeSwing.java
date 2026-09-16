package com.devinsterling.localize.swing;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.event.LocaleChangeEvent;
import com.devinsterling.localize.event.ProviderChangeEvent;
import com.devinsterling.localize.event.Subscription;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Locale;

/// Java Swing [LocalizeSwing] class.
///
/// This class provides reactive bindings to reflect changes
/// automatically whenever the locale or arguments change.
///
/// It is recommended to create a thread-safe [LocalizeSwing]
/// instance through the static factory methods listed here:
/// - [of()]
/// - [of(Locale)]
/// - [of(LocalizeConfig)]
/// - [of(Locale, LocalizeConfig)]
///
/// If a shared-state instance is preferred over an independent instance, [attach] can be used alternatively:
/// ```
/// Localize localize = Localize.of();
/// LocalizeSwing swing = LocalizeSwing.attach(localize);
/// LocalizeFX fx = LocalizeFX.attach(localize); // JavaFX integration: localize-javafx
/// ```
///
/// ### Example
/// Properties file (`messages_en.properties`):
/// ```
/// MyApp.buttonClick = Click to increment
/// MyApp.clickCount = Clicked {click_count, choice, 0 #zero times| 1 #one time| 1 <{click_count} times}!
/// ```
/// Swing code:
/// ```
/// LocalizeSwing localize = LocalizeSwing.of();
/// localize.addBundleProvider("messages");
///
/// JButton button = new JButton();
/// JLabel label = new JLabel();
/// AtomicInteger clickCount = new AtomicInteger();
/// ...
/// localize.bind(button, "MyApp.buttonClick"));
/// localize.get("MyApp.clickCount")
///         .arg("click_count", clickCount::get)
///         .on(Trigger.action(button))
///         .bind(label));
/// ```
/// @since 2.0
public class LocalizeSwing extends Localize {
    private final PropertyChangeManager propertyChangeManager = new PropertyChangeManager();
    private final BindingsManager bindings = new BindingsManager();

    /// Creates a [LocalizeSwing] instance attached to the same internal state as the given source,
    /// sharing the same [locale][getLocale], [formatter][getFormatter], [providers][getProviderEntries],
    /// [configuration][getConfig], [core event listeners][addListener],
    /// and is [notified of events made by either][fireEvent].
    ///
    /// **The given source is not used as a delegate. Method calls will not be routed through it.**
    /// Aside from the shared internal state, both instances are independent.
    ///
    /// @param source Instance with the internal state to attach to.
    /// @throws NullPointerException If `source` is `null`.
    /// @since 2.0
    protected LocalizeSwing(Localize source) {
        super(source);
    }

    /// Creates a [LocalizeSwing] instance with the given locale and configuration.
    ///
    /// @param config Main localize configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected LocalizeSwing(Locale locale, LocalizeConfig config) {
        super(locale, config);
    }

    /// Creates a new [LocalizeSwing] instance attached to the same internal state as the given source,
    /// sharing the same [locale][getLocale], [formatter][getFormatter], [providers][getProviderEntries],
    /// [configuration][getConfig], [core event listeners][addListener],
    /// and is [notified of events made by either][fireEvent].
    ///
    /// **The given source is not used as a delegate. Method calls will not be routed through it.**
    /// Aside from the shared internal state, both instances are independent.
    ///
    /// @param source Instance with the internal state to attach to.
    /// @return       **Thread-safe** LocalizeSwing instance attached to the same internal state of `source`.
    /// @throws NullPointerException If `source` is `null`.
    /// @since 2.0
    public static LocalizeSwing attach(Localize source) {
        return new LocalizeSwing(source);
    }

    /// Creates a new [LocalizeSwing] instance with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** LocalizeSwing instance.
    public static LocalizeSwing of() {
        return of(Locale.getDefault());
    }

    /// Creates a new [LocalizeSwing] instance with the given [Locale] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** LocalizeSwing instance.
    /// @throws NullPointerException If `locale` is `null`.
    public static LocalizeSwing of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Creates a new [LocalizeSwing] instance with the given [LocalizeConfig]
    /// and initial locale set as [Locale#getDefault].
    ///
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeSwing instance.
    /// @throws NullPointerException If `config` is `null`.
    public static LocalizeSwing of(LocalizeConfig config) {
        return of(Locale.getDefault(), config);
    }

    /// Creates a new [LocalizeSwing] instance with the given [Locale] and [LocalizeConfig].
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeSwing instance.
    /// @throws NullPointerException If `locale` or `config` is `null`.
    public static LocalizeSwing of(Locale locale, LocalizeConfig config) {
        return new LocalizeSwing(locale, config);
    }

    /// Adds a listener that is notified whenever the locale changes.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// The emitted [`PropertyChangeEvent`][java.beans.PropertyChangeEvent] has a property name of `locale`.
    /// The old and new values are [Locale] instances.
    /// For a typed change event without the need for casting, see [addLocaleListener].
    ///
    /// ### Note
    /// - If the given listener is `null`, no action is performed and [Subscription#EMPTY] is returned.
    /// - If it is a duplicate, it is not re-inserted and a reference
    ///   to its existing associated [Subscription] is returned.
    ///
    /// ### Memory
    /// The given listener is stored by a strong reference.
    /// To prevent memory leaks when the listener is no longer needed,
    /// call [Subscription#dispose] or [removePropertyChangeListener]:
    ///
    /// ```java
    /// LocalizeSwing localize = new LocalizeSwing(Locale.ENGLISH);
    /// localize.addBundleProvider("sample.text");
    ///
    /// PropertyChangeListener listener = evt -> {
    ///     System.out.println(evt.getNewValue());
    /// }
    /// localize.addPropertyChangeListener(listener);
    ///
    /// // Trigger the listener
    /// localize.setLocale(Locale.JAPANESE);
    ///
    /// // If the listener is no longer needed
    /// localize.removePropertyChangeListener(listener);
    /// ```
    ///
    /// Alternatively, using the subscription model:
    /// ```java
    /// Subscription subscription = localize.addPropertyChangeListener(evt -> {
    ///     System.out.println(evt.getNewValue());
    /// });
    ///
    /// // Trigger the listener
    /// localize.setLocale(Locale.KOREAN);
    ///
    /// // If the listener is no longer needed
    /// subscription.dispose();
    /// ```
    /// @param listener Listener to add.
    /// @return         Subscription to remove the added listener,
    ///                 for use on the Swing UI (EDT) thread only.
    /// @see #addLocaleListener(LocaleChangeListener)
    // An NPE is not thrown to match Swing's patterns regarding potentially null arguments.
    public Subscription addPropertyChangeListener(PropertyChangeListener listener) {
        return propertyChangeManager.addPropertyChangeListener(listener);
    }

    /// Removes a listener that was [previously added][addPropertyChangeListener].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// If the given listener is `null`, no action is performed.
    ///
    /// @param listener Listener to remove.
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeManager.removePropertyChangeListener(listener);
    }

    /// Adds a listener that is notified whenever the locale changes.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// The emitted [`PropertyChangeEvent`][java.beans.PropertyChangeEvent] has a property name of `locale`.
    /// The old and new values are [Locale] instances.
    /// For a typed change event without the need for casting, see [addLocaleListener].
    ///
    /// ### Note
    /// - If the given listener is `null`, no action is performed and [Subscription#EMPTY] is returned.
    /// - If it is a duplicate, it is not re-inserted and a reference
    ///   to its existing associated [Subscription] is returned.
    ///
    /// ### Memory
    /// The given listener is stored by a strong reference.
    /// To prevent memory leaks when the listener is no longer needed,
    /// call [Subscription#dispose] or [removePropertyChangeListener]:
    ///
    /// ```java
    /// LocalizeSwing localize = new LocalizeSwing(Locale.ENGLISH);
    /// localize.addBundleProvider("sample.text");
    ///
    /// PropertyChangeListener listener = (oldLocale, newLocale) -> {
    ///     System.out.println(newLocale);
    /// }
    /// localize.addLocaleListener(listener);
    ///
    /// // Trigger the listener
    /// localize.setLocale(Locale.JAPANESE);
    ///
    /// // If the listener is no longer needed
    /// localize.removeLocaleListener(listener);
    /// ```
    ///
    /// Alternatively, using the subscription model:
    /// ```java
    /// Subscription subscription = localize.addLocaleListener((oldLocale, newLocale) -> {
    ///     System.out.println(newLocale);
    /// });
    ///
    /// // Trigger the listener
    /// localize.setLocale(Locale.KOREAN);
    ///
    /// // If the listener is no longer needed
    /// subscription.dispose();
    /// ```
    /// @param listener Listener to add.
    /// @return         Subscription to remove the added listener,
    ///                 for use on the Swing UI (EDT) thread only.
    public Subscription addLocaleListener(LocaleChangeListener listener) {
        if (listener == null) {
            return Subscription.EMPTY;
        }
        return addPropertyChangeListener(new LocaleChangeListenerAdapter(listener));
    }

    /// Removes a listener that was [previously added][addLocaleListener].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// If the given listener is `null`, no action is performed.
    ///
    /// @param listener Listener to remove.
    public void removeLocaleListener(LocaleChangeListener listener) {
        if (listener == null) return;

        removePropertyChangeListener(new LocaleChangeListenerAdapter(listener));
    }

    /// Returns the registry that updates all registered bindings whenever the locale changes.
    ///
    /// @return Locale binding registry.
    protected BindingRegistry getBindingRegistry() {
        return bindings;
    }

    /// Triggers all property listeners to fire and string bindings to update whenever the locale changes.
    ///
    /// This method is thread-safe, dispatching to the Swing UI (EDT) thread when needed.
    protected void notifyListeners() {
        if (SwingUtilities.isEventDispatchThread()) {
            bindings.notifyListeners();
        } else {
            SwingUtilities.invokeLater(bindings::notifyListeners);
        }
    }

    @Override protected void onLocaleChanged(LocaleChangeEvent change) {
        if (SwingUtilities.isEventDispatchThread()) {
            onLocaleChangedEdtThread(change);
        } else {
            SwingUtilities.invokeLater(() -> onLocaleChangedEdtThread(change));
        }
    }

    private void onLocaleChangedEdtThread(LocaleChangeEvent change) {
        if (change.isValid()) {
            notifyListeners();
        }
        // After notifying all listeners, recheck if the held locale is still the current one
        if (change.isValid()) {
            propertyChangeManager.notifyChangeListeners(this, change.getOldLocale(), change.getNewLocale());
        }
    }

    // In a future release, events will be used to optimize provider-scoped listeners (uncommon case)
    @Override protected void onProvidersChanged(ProviderChangeEvent event) {
        // Optimization NOTE:
        // Ignore any events caused by a locale change; it is already handled on `onLocaleChanged`.
        // This avoids unnecessarily recomputing bindings more than once in quick succession.
        if (event.getCause() != ProviderChangeEvent.Cause.LOCALE_CHANGE) {
            notifyListeners();
        }
    }

    @Override public SwingLocalizationValueBuilder<?> format(String pattern) {
        return new SwingLocalizationValueBuilder<>(new LocalizationRequestSource.Pattern(pattern), this);
    }

    @Override public SwingLocalizationValueBuilder<?> get(String key) {
        return new SwingLocalizationValueBuilder<>(new LocalizationRequestSource.Key(key), this);
    }

    @Override public SwingLocalizationValueBuilder<?> get(LocalizationKey key) {
        return get(key.getKey());
    }

    /// Binds the given component to the associated resource value.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// The component's text is automatically updated whenever a refresh occurs or the locale changes.
    /// For a list of supported components, see [SwingLocalizationValueBuilder#bind(Component)].
    ///
    /// @param key       Resource bundle key associated with the value to bind.
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    /// @throws IllegalArgumentException If `component` is an unsupported type.
    /// @see SwingLocalizationValueBuilder#bind(Component)
    public StringBinding bind(String key, Component component) {
        return get(key).bind(component);
    }

    /// Binds the given component to the associated resource value.
    ///
    /// This method is equivalent to [bind(String, Component)].
    /// For a list of supported components, see [SwingLocalizationValueBuilder#bind(Component)].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// @param key       Resource bundle key associated with the value to bind.
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    /// @throws IllegalArgumentException If `component` is an unsupported type.
    /// @see SwingLocalizationValueBuilder#bind(Component)
    public StringBinding bind(LocalizationKey key, Component component) {
        return get(key).bind(component);
    }

    /// Binds the given component's [tooltip][JComponent#setToolTipText] to the associated resource value.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// Component tooltip text is automatically updated whenever a refresh occurs or the locale changes.
    ///
    /// @param key       Resource bundle key associated with the value to bind.
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    public StringBinding bindTooltip(String key, JComponent component) {
        return get(key).bindTooltip(component);
    }

    /// Binds the given component's [tooltip][JComponent#setToolTipText] to the associated resource value.
    ///
    /// This method is equivalent to [bindTooltip(String, JComponent)].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// @param key       Resource bundle key associated with the value to bind.
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    public StringBinding bindTooltip(LocalizationKey key, JComponent component) {
        return get(key).bindTooltip(component);
    }
}
