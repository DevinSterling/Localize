package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.Objects;

/// JavaFX [Localize] class.
///
/// It is recommended to create a thread-safe [LocalizeFX]
/// instance through the static factory methods listed here:
/// - [#of()]
/// - [#of(Locale)]
/// - [#of(Locale, LocalizeConfig)]
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

    /// Create a [LocalizeFX] instance with the desired configuration.
    ///
    /// @param config The configuration.
    protected LocalizeFX(LocalizeConfig config) {
        super(config);
    }

    /// Locale associated with this [Localize] instance.
    /// Each time the locale is changed, all providers are refreshed.
    ///
    /// When not on the FX application thread, it is recommended to
    /// use [#getLocale()] and [#setLocale(Locale)] instead.
    ///
    /// @return **Non-thread-safe** observable locale property.
    /// @see #setLocale(Locale)
    /// @see #getLocale()
    /// @see #refresh()
    public abstract ObjectProperty<Locale> localeProperty();

    /// Triggers the locale property to emit an invalidation event
    /// to listeners, triggering all string bindings to update.
    protected abstract void notifyListeners();

    /// Identical functionality as [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** LocalizeFX instance.
    public static LocalizeFX of() {
        return of(Locale.getDefault());
    }

    /// Identical functionality as [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** LocalizeFX instance.
    public static LocalizeFX of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Create a new [LocalizeFX] instance with a given
    /// [LocalizeFX] and [LocalizeConfig].
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeFX instance.
    public static LocalizeFX of(Locale locale, LocalizeConfig config) {
        assertLocale(locale);
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

    private static void assertLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
    }

    private static class LocalizeFXImpl extends LocalizeFX {
        private final LocaleProperty localeProperty;
        private volatile Locale locale;

        private LocalizeFXImpl(Locale locale, LocalizeConfig config) {
            super(config);
            this.locale = locale;
            this.localeProperty = new LocaleProperty(locale);
        }

        @Override protected void notifyListeners() {
            if (FXThread.isUIThread()) {
                localeProperty.forceFireValueChanged();
            } else {
                FXThread.onUIThread(localeProperty::forceFireValueChanged);
            }
        }

        @Override public ObjectProperty<Locale> localeProperty() {
            return localeProperty;
        }

        @Override public void setLocale(Locale locale) {
            if (FXThread.isUIThread()) {
                // Assertion/refreshing is handled within this method
                localeProperty.set(locale);
            } else if (!this.locale.equals(locale)) {
                assertLocale(locale);
                this.locale = locale;
                refresh(locale);
                FXThread.onUIThread(() -> localeProperty.setWithoutRefresh(locale));
            }
        }

        @Override public Locale getLocale() {
            return FXThread.isUIThread() ? localeProperty.get() : locale;
        }

        private class LocaleProperty extends SimpleObjectProperty<Locale> {
            private LocaleProperty(Locale locale) {
                super(locale);
            }

            @Override public void set(Locale locale) {
                if (get().equals(locale)) return;

                assertLocale(locale);
                LocalizeFXImpl.this.locale = locale;
                refresh(locale); // Eagerly refresh
                super.set(locale);
            }

            private void setWithoutRefresh(Locale locale) {
                super.set(locale);
            }

            private void forceFireValueChanged() {
                fireValueChangedEvent();
            }
        }
    }
}
