package com.devinsterling.localize.fx;

import com.devinsterling.localize.LocalizationKey;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.ResourceBundleProvider;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/// JavaFX [Localize] class.
///
/// It is recommended to create a thread-safe [LocalizeFX]
/// instance through the static factory methods listed here:
/// - [#of()]
/// - [#of(Locale)]
/// - [#of(LocalizeConfig)]
/// - [#of(Locale, LocalizeConfig)]
///
/// This class provides an observable string binding
/// to reflect changes automatically whenever the
/// locale or any arguments change.
///
/// ### Example
/// Properties file (`messages_en.properties`):
/// ```
/// MyApp.buttonClick = Click to increment
/// MyApp.clickCount = Clicked {click_count, plural, 0={zero times} 1={one time} other{# times}}!
/// ```
/// JavaFX code:
/// ```
/// LocalizeFX localize = LocalizeFX.of();
/// localize.addBundleProvider(locale -> ResourceBundle.getBundle("messages", locale));
///
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

    /// Creates a [LocalizeFX] instance with the desired configuration.
    ///
    /// @param config The configuration.
    /// @throws NullPointerException If `config` is `null`.
    protected LocalizeFX(LocalizeConfig config) {
        super(config);
    }

    /// The current locale.
    ///
    /// Each time the locale is changed, all providers are refreshed.
    ///
    /// When not on the FX application thread, it is recommended
    /// to use [#getLocale()] and [#setLocale(Locale)] instead.
    ///
    /// @return **Non-thread-safe** observable locale property.
    /// @see #setLocale(Locale)
    /// @see #getLocale()
    /// @see #refresh()
    public abstract ObjectProperty<Locale> localeProperty();

    /// Triggers the locale property to emit an invalidation event
    /// to listeners, triggering all string bindings to update.
    protected abstract void notifyListeners();

    /// Equivalent to [#of(Locale, LocalizeConfig)] with the
    /// initial locale set as [Locale#getDefault()] and default configuration.
    ///
    /// @return **Thread-safe** LocalizeFX instance.
    public static LocalizeFX of() {
        return of(Locale.getDefault());
    }

    /// Equivalent to [#of(Locale, LocalizeConfig)] with a given
    /// [Locale] and default configuration.
    ///
    /// @param locale Initial locale.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `locale` is `null`.
    public static LocalizeFX of(Locale locale) {
        return of(locale, new LocalizeConfig());
    }

    /// Equivalent to [#of(Locale, LocalizeConfig)] with a given
    /// [LocalizeConfig] and initial locale set as [Locale#getDefault()].
    ///
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `config` is `null`.
    /// @since 1.2
    public static LocalizeFX of(LocalizeConfig config) {
        return of(Locale.getDefault(), config);
    }

    /// Creates a new [LocalizeFX] instance with a given [LocalizeFX] and [LocalizeConfig].
    ///
    /// @param locale Initial locale.
    /// @param config Initial Configuration.
    /// @return       **Thread-safe** LocalizeFX instance.
    /// @throws NullPointerException If `locale` or `config` is `null`.
    public static LocalizeFX of(Locale locale, LocalizeConfig config) {
        return new LocalizeFXImpl(assertLocale(locale), config);
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Adding providers will update any active string bindings (e.g., from [#getBinding(String)]).
    @Override public boolean putBundleProvider(String key, ResourceBundleProvider provider) {
        boolean isNewProvider = super.putBundleProvider(key, provider);
        notifyListeners();
        return isNewProvider;
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Adding providers will update any active string bindings (e.g., from [#getBinding(String)]).
    @Override public String addBundleProvider(ResourceBundleProvider provider) {
        String key = super.addBundleProvider(provider);
        notifyListeners();
        return key;
    }

    /// {@inheritDoc}
    ///
    /// ### Note
    /// Removing providers will update any active string bindings (e.g., from [#getBinding(String)]).
    @Override public boolean removeBundleProvider(String key) {
        boolean isRemoved = super.removeBundleProvider(key);

        if (isRemoved) {
            notifyListeners();
        }

        return isRemoved;
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

    /// Retrieves an observable string binding.
    ///
    /// ### Example Usage
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

    /// Equivalent to [#getBinding(String)].
    ///
    /// @param  key Resource bundle key.
    /// @return     Observable string binding.
    /// @throws NullPointerException If `key` is `null`.
    /// @see #getValue(LocalizationKey)
    public StringBinding getBinding(LocalizationKey key) {
        return getBinding(key.getKey());
    }

    private static Locale assertLocale(Locale locale) {
        return Objects.requireNonNull(locale, "locale must not be null");
    }

    private static final class LocalizeFXImpl extends LocalizeFX {
        private final LocaleProperty localeProperty;
        private final VersionedLocale locale;

        private LocalizeFXImpl(Locale locale, LocalizeConfig config) {
            super(config);
            this.locale = new VersionedLocale(locale);
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
                // Refreshing is handled within this method
                localeProperty.set(locale);
                return;
            }

            long version = this.locale.set(locale);
            // If the version is `-1`, the locale is equivalent.
            if (version < 0) return;

            refresh(locale);
            // Avoid setting the property to a stale `Locale` by checking the version
            FXThread.onUIThread(() -> {
                if (this.locale.isCurrent(version)) {
                    localeProperty.setWithoutRefresh(locale);
                }
            });
        }

        @Override public Locale getLocale() {
            return locale.get();
        }

        /// NOTE: All methods of this class must be called from the JavaFX UI thread, if available.
        private class LocaleProperty extends SimpleObjectProperty<Locale> {
            private LocaleProperty(Locale locale) {
                super(locale);
            }

            @Override public void set(Locale locale) {
                // Do not update the locale if it's equivalent to the current one
                if (LocalizeFXImpl.this.locale.set(assertLocale(locale)) < 0) return;

                // Eagerly refresh bundles first before triggering listeners
                refresh(locale);
                markValid();
                super.set(locale);
            }

            private void setWithoutRefresh(Locale locale) {
                markValid();
                super.set(locale);
            }

            private void forceFireValueChanged() {
                fireValueChangedEvent();
            }

            /// Marks the internal [SimpleObjectProperty] private field `valid` to `true`.
            ///
            /// - Ensures locale changes are eagerly propagated when calling `super.set`.
            /// - Avoids firing duplicate events compared to [#forceFireValueChanged] (if already marked valid).
            private void markValid() {
                // Force SimpleObjectProperty to be valid, internally does `valid = true;`
                get();
            }
        }

        private static final class VersionedLocale {
            private final AtomicLong version = new AtomicLong();
            private final AtomicReference<Locale> locale;

            private VersionedLocale(Locale locale) {
                this.locale = new AtomicReference<>(assertLocale(locale));
            }

            /// @return The new version, or `-1` if the given locale is equivalent.
            private synchronized long set(Locale newLocale) {
                // If the given `newLocale` is equivalent to the current `locale`,
                // no replacement is performed, matching `LocalizeImpl#setLocale`.
                if (locale.get().equals(assertLocale(newLocale))) return -1;

                locale.set(newLocale);
                return version.incrementAndGet();
            }

            private Locale get() {
                return locale.get();
            }

            private boolean isCurrent(long version) {
                return version == this.version.get();
            }
        }
    }
}
