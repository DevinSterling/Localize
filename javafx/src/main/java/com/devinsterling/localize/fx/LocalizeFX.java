package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/// JavaFX [Localize] class.
///
/// It is recommended to create a [LocalizeFX] instance
/// through the static factory methods listed here:
/// - [#of()]
/// - [#of(Locale)]
/// - [#of(Locale, LocalizeConfig)]
///
/// **The default implementation is not thread-safe**.
///
/// This class provides an observable string binding
/// to reflect changes automatically whenever the
/// locale or any arguments change.
///
/// ### Example
/// properties file:
/// ```
/// MyApp.buttonClick = "Click to increment"
/// MyApp.clickCount = "Clicked {click_count, plural, 0={zero times} 1={one time} other{# times}}!"
/// ```
/// JavaFX code:
/// ```
/// Button button = new Button();
/// Label label = new Label();
/// DoubleProperty clickCount = new SimpleDoubleProperty();
/// ...
/// button.textProperty().bind(localize.getBinding("MyApp.buttonClick"));
/// label.textProperty().bind(localize.get("MyApp.clickCount")
///                                   .argument("click_count", clickCount)
///                                   .binding());
///
/// ```
/// @since 1.0
public abstract class LocalizeFX extends Localize {

    /// Creates a [LocalizeFX] instance with the default backing map.
    protected LocalizeFX() {}

    /// Create a [LocalizeFX] instance with the desired backing map.
    ///
    /// @param supplier The backing map that stores resource bundle entries.
    protected LocalizeFX(Supplier<Map<String, BundleEntry>> supplier) {
        super(supplier);
    }

    /// Locale associated with this [Localize] instance.
    /// Each time the locale is changed, all providers are refreshed.
    ///
    /// Attempts to set this property to `null` directly will be transformed
    /// into [Locale#getDefault()].
    ///
    /// @return Observable locale property.
    /// @see #refresh()
    public abstract ObjectProperty<Locale> localeProperty();

    /// Triggers the locale property to emit an invalidation event
    /// to listeners, triggering all string bindings to update.
    protected abstract void notifyListeners();

    /// Identical functionality as [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return Created Localize instance.
    public static LocalizeFX of() {
        return of(Locale.getDefault());
    }

    /// Identical functionality as [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       Created Localize instance.
    public static LocalizeFX of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Create a new [LocalizeFX] instance with a given
    /// [LocalizeFX] and [LocalizeConfig].
    ///
    /// The created instance **is not thread-safe**.
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       Created Localize instance.
    public static LocalizeFX of(Locale locale, LocalizeConfig config) {
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return new LocalizeFXImpl(locale, config);
    }

    /// {@inheritDoc}
    @Override public boolean refresh(String key) {
        boolean isRefreshed = super.refresh(key);

        if (isRefreshed) {
            notifyListeners();
        }

        return isRefreshed;
    }

    /// {@inheritDoc}
    @Override public void refresh() {
        super.refresh();
        notifyListeners();
    }

    /// {@inheritDoc}
    @Override public FXLocalizationValueBuilder<?> get(String key) {
        return new FXLocalizationValueBuilder<>(key, localeProperty(), this::applyBuilderProperties);
    }

    /// {@inheritDoc}
    @Override public FXLocalizationValueBuilder<?> get(LocalizationKey key) {
        return get(key.getKey());
    }

    /// Retrieve an observable string binding.
    ///
    /// ### Example Usage:
    /// ```
    /// Button button = new Button();
    /// button.textProperty().bind(localize.getBinding("MyApp.button"));
    /// ```
    ///
    /// @param key Resource bundle key.
    /// @return    String binding that is updated whenever
    ///            a refresh occurs or the locale changes.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(String)
    public StringBinding getBinding(String key) {
        return get(key).binding();
    }

    /// Identical functionality as [#getBinding(String)].
    ///
    /// @param  key Resource bundle key.
    /// @return     Observable string binding.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(LocalizationKey)
    public StringBinding getBinding(LocalizationKey key) {
        return getBinding(key.getKey());
    }

    private static final class LocalizeFXImpl extends LocalizeFX {
        private final LocaleProperty locale;

        private LocalizeFXImpl(Locale locale, LocalizeConfig config) {
            this.locale = new LocaleProperty(locale);
            setConfig(config);
        }

        @Override protected void notifyListeners() {
            locale.forceFireValueChanged();
        }

        @Override public ObjectProperty<Locale> localeProperty() {
            return locale;
        }

        @Override public void setLocale(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null");
            this.locale.set(locale);
        }

        @Override public Locale getLocale() {
            // Locale must not be null
            if (locale.get() == null) {
                setLocale(Locale.getDefault());
            }
            return locale.get();
        }
    }

    private final class LocaleProperty extends SimpleObjectProperty<Locale> {

        public LocaleProperty(Locale locale) {
            super(locale);
        }

        @Override protected void invalidated() {
            if (get() != null) {
                refresh(get());
            }
        }

        @Override public void set(Locale locale) {
            super.set(locale == null ? Locale.getDefault() : locale);
        }

        public void forceFireValueChanged() {
            fireValueChangedEvent();
        }
    }
}
