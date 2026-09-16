package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.event.LocaleChangeEvent;
import com.devinsterling.localize.event.ProviderChangeEvent;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;

/// JavaFX [Localize] class.
///
/// This class provides an observable string binding to reflect
/// changes automatically whenever the locale or any arguments change.
///
/// It is recommended to create a thread-safe [LocalizeFX]
/// instance through the static factory methods listed here:
/// - [of()]
/// - [of(Locale)]
/// - [of(LocalizeConfig)]
/// - [of(Locale, LocalizeConfig)]
///
/// If a shared-state instance is preferred over an independent instance, [attach] can be used alternatively:
/// ```java
/// Localize localize = Localize.of();
/// LocalizeFX fx = LocalizeFX.attach(localize);
/// LocalizeSwing swing = LocalizeSwing.attach(localize); // Swing integration: localize-swing
/// ```
///
/// ### Example
/// Properties file (`messages_en.properties`):
/// ```
/// MyApp.buttonClick = Click to increment
/// MyApp.clickCount = Clicked {click_count, choice, 0 #zero times| 1 #one time| 1 <{click_count} times}!
/// ```
/// JavaFX code:
/// ```
/// LocalizeFX localize = LocalizeFX.of();
/// localize.addProvider("messages");
///
/// Button button = new Button();
/// Label label = new Label();
/// DoubleProperty clickCount = new SimpleDoubleProperty();
/// ...
/// button.textProperty().bind(localize.getBinding("MyApp.buttonClick"));
/// label.textProperty().bind(localize.get("MyApp.clickCount")
///                                   .arg("click_count", clickCount)
///                                   .binding());
///
/// ```
/// @since 1.0
public class LocalizeFX extends Localize {
    private final LocaleProperty localeProperty;

    /// Creates a [LocalizeFX] instance attached to the same internal state as the given source,
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
    protected LocalizeFX(Localize source) {
        super(source);
        this.localeProperty = new LocaleProperty(source.getLocale());
    }

    /// Creates a [LocalizeFX] instance with the given locale and configuration.
    ///
    /// @param locale Initial locale.
    /// @param config Main localize configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected LocalizeFX(Locale locale, LocalizeConfig config) {
        super(locale, config);
        this.localeProperty = new LocaleProperty(locale);
    }

    /// Creates a new [LocalizeFX] instance attached to the same internal state as the given source,
    /// sharing the same [locale][getLocale], [formatter][getFormatter], [providers][getProviderEntries],
    /// [configuration][getConfig], [core event listeners][addListener],
    /// and is [notified of events made by either][fireEvent].
    ///
    /// **The given source is not used as a delegate. Method calls will not be routed through it.**
    /// Aside from the shared internal state, both instances are independent.
    ///
    /// @param source Instance with the internal state to attach to.
    /// @return       **Thread-safe** LocalizeFX instance attached to the same internal state of `source`.
    /// @throws NullPointerException If `source` is `null`.
    /// @since 2.0
    public static LocalizeFX attach(Localize source) {
        return new LocalizeFX(source);
    }

    /// Creates a new [LocalizeFX] instance with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** LocalizeFX instance.
    public static LocalizeFX of() {
        return of(Locale.getDefault());
    }

    /// Creates a new [LocalizeFX] instance with the given [Locale] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `locale` is `null`.
    public static LocalizeFX of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Creates a new [LocalizeFX] instance with the given [LocalizeConfig]
    /// and initial locale set as [Locale#getDefault].
    ///
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `config` is `null`.
    /// @since 1.2
    public static LocalizeFX of(LocalizeConfig config) {
        return of(Locale.getDefault(), config);
    }

    /// Creates a new [LocalizeFX] instance with the given [Locale] and [LocalizeConfig].
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `locale` or `config` is `null`.
    public static LocalizeFX of(Locale locale, LocalizeConfig config) {
        return new LocalizeFX(locale, config);
    }

    /// The current locale.
    ///
    /// Each time the locale is changed, all providers are refreshed.
    ///
    /// **This method is intended to be called on the JavaFX Application thread only.**
    /// When not on that thread, it is recommended to use [getLocale] and [setLocale] instead,
    /// which are thread-safe.
    ///
    /// @return **Non-thread-safe** observable locale property.
    /// @see setLocale
    /// @see getLocale
    /// @see refreshProviders()
    public ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    /// Triggers the locale property to emit an invalidation event
    /// to listeners, triggering all string bindings to update.
    ///
    /// This method is thread-safe, dispatching to the JavaFX application thread when needed.
    protected void notifyListeners() {
        if (Platform.isFxApplicationThread()) {
            localeProperty.forceFireValueChanged();
        } else {
            Platform.runLater(localeProperty::forceFireValueChanged);
        }
    }

    @Override protected void onLocaleChanged(LocaleChangeEvent change) {
        if (Platform.isFxApplicationThread()) {
            onLocaleChangedFxThread(change);
        } else if (change.isValid()) {
            Platform.runLater(() -> onLocaleChangedFxThread(change));
        }
    }

    private void onLocaleChangedFxThread(LocaleChangeEvent change) {
        // Avoid setting the property to a stale `Locale` by checking the version
        if (change.isValid()) {
            localeProperty.setInternal(change.getNewLocale());
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

    @Override public FXLocalizationValueBuilder<?> format(String pattern) {
        return new FXLocalizationValueBuilder<>(new LocalizationRequestSource.Pattern(pattern), this);
    }

    @Override public FXLocalizationValueBuilder<?> get(String key) {
        return new FXLocalizationValueBuilder<>(new LocalizationRequestSource.Key(key), this);
    }

    @Override public FXLocalizationValueBuilder<?> get(LocalizationKey key) {
        return get(key.getKey());
    }

    /// Retrieves an observable string bound to the associated resource value.
    ///
    /// **This method is intended to be called on the JavaFX Application thread only.**
    ///
    /// ### Example Usage
    /// ```
    /// Button button = new Button();
    /// button.textProperty().bind(localize.getBinding("MyApp.button"));
    /// ```
    ///
    /// @param key Resource bundle key associated with the value to bind.
    /// @return    String binding that is updated whenever a refresh occurs or the locale changes.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(String)
    public StringBinding getBinding(String key) {
        return Bindings.createStringBinding(() -> getValue(key), localeProperty());
    }

    /// Retrieves an observable string bound to the associated resource value.
    ///
    /// This method is equivalent to [getBinding(String)].
    ///
    /// **This method is intended to be called on the JavaFX Application thread only.**
    ///
    /// @param key Resource bundle key associated with the value to bind.
    /// @return    String binding that is updated whenever a refresh occurs or the locale changes.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(LocalizationKey)
    public StringBinding getBinding(LocalizationKey key) {
        return getBinding(key.getKey());
    }

    /// NOTE: All methods of this class must be called from the JavaFX UI thread, if available.
    private class LocaleProperty extends SimpleObjectProperty<Locale> {
        private LocaleProperty(Locale locale) {
            super(locale);
        }

        /// This method is only ever called externally.
        /// Internal calls to set the locale property are delegated to [setInternal].
        @Override public void set(Locale locale) {
            // This method will delegate to `setInternal`
            LocalizeFX.this.setLocale(locale);
        }

        private void setInternal(Locale locale) {
            markValid();
            super.set(locale);
        }

        private void forceFireValueChanged() {
            fireValueChangedEvent();
        }

        /// Marks the internal [SimpleObjectProperty] private field `valid` to `true`.
        ///
        /// - Ensures locale changes are eagerly propagated when calling `super.set`.
        /// - Avoids firing duplicate events compared to [forceFireValueChanged] (if already marked valid).
        private void markValid() {
            // Force SimpleObjectProperty to be valid, internally does `valid = true;`
            get();
        }
    }
}
