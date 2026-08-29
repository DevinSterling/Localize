package com.devinsterling.localize.fx;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.LocalizationValueBuilder;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// Builder to provide an observable string binding
/// of a formatted localized value.
///
/// @param <B> Builder instance type.
/// @since 1.0
public class FXLocalizationValueBuilder<B extends FXLocalizationValueBuilder<B>> extends LocalizationValueBuilder<B> {

    /// Creates a builder to request a specified localized binding.
    ///
    /// @param source   Source to derive a formatted localized value from.
    /// @param localize Localization instance to handle requests.
    /// @throws NullPointerException if `source` or `localize` is `null`.
    protected FXLocalizationValueBuilder(LocalizationRequestSource source, LocalizeFX localize) {
        super(source, localize);
    }

    @Override public Arguments.Resolver getResolver() {
        return FXArgumentsResolver.INSTANCE;
    }

    /// Retrieves an observable formatted string with all properties applied from this builder.
    ///
    /// The binding is automatically updated when any of the passed observable arguments or the locale changes.
    ///
    /// @return The observable formatted localized value, **intended for the FX application thread only**.
    public StringBinding binding() {
        LocalizeFX localize = getLocalize();
        // Effectively final variables to prevent implicit reference to this class
        LocalizationRequestSource source = getSource();
        String defaultValue = getDefaultValue();
        Arguments arguments = snapshotArguments();
        Arguments.Resolver resolver = getResolver();

        return Bindings.createStringBinding(
            () -> localize.formatValue(
                LocalizationRequest.Builder
                    .of(source)
                    .defaultValue(defaultValue)
                    .arguments(arguments.resolve(resolver))
                    .build()
            ),
            getObservables(localize.localeProperty(), arguments)
        );
    }

    protected LocalizeFX getLocalize() {
        return (LocalizeFX) super.getLocalize();
    }

    /// @return An array containing provided `locale` + all extracted observables from `arguments`.
    private static Observable[] getObservables(Observable locale, Arguments arguments) {
        // Set to avoid duplicate observables
        Set<Observable> observables = null;

        for (Object value : arguments.values()) {
            if (value instanceof Observable observable) {
                if (observables == null) {
                    // In most cases, the majority of arguments are observables
                    observables = new HashSet<>(arguments.size() + 1);
                    observables.add(locale);
                }

                observables.add(observable);
            }
        }

        return observables == null ? new Observable[] { locale } : observables.toArray(new Observable[0]);
    }
}
