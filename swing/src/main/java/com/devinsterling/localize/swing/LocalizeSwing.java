package com.devinsterling.localize.swing;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.ResourceBundleProvider;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/// Java Swing [LocalizeSwing] class.
///
/// It is recommended to create a thread-safe [LocalizeSwing]
/// instance through the static factory methods listed here:
/// - [#of()]
/// - [#of(Locale)]
/// - [#of(LocalizeConfig)]
/// - [#of(Locale, LocalizeConfig)]
///
/// This class provides reactive bindings to reflect changes
/// automatically whenever the locale or arguments change.
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
public abstract class LocalizeSwing extends Localize {

    /// Creates a [LocalizeSwing] instance with the given configuration.
    ///
    /// @param config Main localize configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected LocalizeSwing(LocalizeConfig config) {
        super(config);
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
    /// @return         Subscription to remove the added listener.
    /// @see #addLocaleListener(LocaleChangeListener)
    // An NPE is not thrown to match Swing's patterns regarding potentially null arguments.
    public abstract Subscription addPropertyChangeListener(PropertyChangeListener listener);

    /// Removes a listener that was [previously added][addPropertyChangeListener].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// If the given listener is `null`, no action is performed.
    ///
    /// @param listener Listener to remove.
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

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
    /// @return         Subscription to remove the added listener.
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
    protected abstract BindingRegistry getBindingRegistry();

    /// Triggers all property listeners to fire and string bindings to update whenever the locale changes.
    ///
    /// @implSpec This method must be thread-safe, dispatching to the Swing UI (EDT) thread when needed.
    protected abstract void notifyListeners();

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
        return new LocalizeSwingImpl(assertLocale(locale), config);
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Adding providers will update any active text bindings (e.g., from [#bind(String, Component)]).
    @Override public boolean putBundleProvider(String key, ResourceBundleProvider provider) {
        boolean isNewProvider = super.putBundleProvider(key, provider);
        notifyListeners();
        return isNewProvider;
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Adding providers will update any active text bindings (e.g., from [#bind(String, Component)]).
    @Override public String addBundleProvider(ResourceBundleProvider provider) {
        String key = super.addBundleProvider(provider);
        notifyListeners();
        return key;
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Removing providers will update any active text bindings (e.g., from [#bind(String, Component)]).
    @Override public boolean removeBundleProvider(String key) {
        boolean isRemoved = super.removeBundleProvider(key);

        if (isRemoved) {
            notifyListeners();
        }

        return isRemoved;
    }

    @Override public boolean refresh(String key) {
        boolean isRefreshed = super.refresh(key);

        if (isRefreshed) {
            notifyListeners();
        }

        return isRefreshed;
    }

    @Override public void refresh() {
        super.refresh();
        notifyListeners();
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
    /// This method is equivalent to [#bind(String, Component)].
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
    /// This method is equivalent to [#bindTooltip(String, JComponent)].
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

    private static Locale assertLocale(Locale locale) {
        return Objects.requireNonNull(locale, "locale must not be null");
    }

    private static final class LocalizeSwingImpl extends LocalizeSwing {
        private final PropertyChangeManager propertyChangeManager = new PropertyChangeManager();
        private final BindingsManager bindings = new BindingsManager();
        private final VersionedLocale locale;

        public LocalizeSwingImpl(Locale locale, LocalizeConfig config) {
            super(config);
            this.locale = new VersionedLocale(locale);
        }

        @Override public void setLocale(Locale locale) {
            VersionedLocale.SetResult setResult = this.locale.set(locale);
            // If unchanged, the locale is equivalent.
            if (setResult.isUnchanged()) return;

            refresh(locale);

            if (SwingUtilities.isEventDispatchThread() && this.locale.isCurrent(setResult)) {
                notifyListeners();
                propertyChangeManager.notifyChangeListeners(this, setResult.previous, locale);
            } else {
                SwingUtilities.invokeLater(() -> {
                    if (this.locale.isCurrent(setResult)) {
                        notifyListeners();
                        propertyChangeManager.notifyChangeListeners(this, locale, locale);
                    }
                });
            }
        }

        @Override public Locale getLocale() {
            return locale.get();
        }

        @Override public Subscription addPropertyChangeListener(PropertyChangeListener changeListener) {
            return propertyChangeManager.addPropertyChangeListener(changeListener);
        }

        @Override public void removePropertyChangeListener(PropertyChangeListener changeListener) {
            propertyChangeManager.removePropertyChangeListener(changeListener);
        }

        @Override protected BindingRegistry getBindingRegistry() {
            return bindings;
        }

        @Override protected void notifyListeners() {
            if (SwingUtilities.isEventDispatchThread()) {
                bindings.notifyListeners();
            } else {
                SwingUtilities.invokeLater(bindings::notifyListeners);
            }
        }

        private static final class VersionedLocale {
            private final AtomicLong version = new AtomicLong();
            private final AtomicReference<Locale> locale;

            private VersionedLocale(Locale locale) {
                this.locale = new AtomicReference<>(assertLocale(locale));
            }

            private synchronized SetResult set(Locale newLocale) {
                Locale current = locale.get();

                // If the given `newLocale` is equivalent to the current `locale`,
                // no replacement is performed, matching `LocalizeImpl#setLocale`.
                if (current.equals(assertLocale(newLocale))) {
                    // version is `-1` if the given locale is equivalent.
                    return SetResult.unchanged(current);
                } else {
                    locale.set(newLocale);
                    return new SetResult(current, version.incrementAndGet());
                }
            }

            private Locale get() {
                return locale.get();
            }

            private boolean isCurrent(SetResult version) {
                return version.version == this.version.get();
            }

            private record SetResult(Locale previous, long version) {
                static SetResult unchanged(Locale previous) {
                    return new SetResult(previous, -1);
                }

                private boolean isUnchanged() {
                    return version == -1;
                }
            }
        }
    }
}
