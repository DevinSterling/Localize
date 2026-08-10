package com.devinsterling.localize.fx;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationRequest;
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
    private final ObservableValue<?> locale;

    /// Creates a builder to request a specified localized binding.
    ///
    /// @param key     Key to request a formatted localized value for.
    /// @param locale  Observable of the selected locale.
    /// @param applier Callback to apply the properties of this builder
    ///                to the requested value.
    public FXLocalizationValueBuilder(String key, ObservableValue<?> locale, Applier applier) {
        super(key, applier);
        this.locale = locale;
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
        // Effectively final variables to prevent implicit reference to this class
        String key = getKey();
        String defaultValue = getDefaultValue();
        Applier applier = getApplier();
        Arguments arguments = snapshotArguments();
        Arguments.Resolver resolver = getResolver();

        return Bindings.createStringBinding(
            () -> applier.evaluate(
                LocalizationRequest.Builder
                    .of(key)
                    .defaultValue(defaultValue)
                    .arguments(arguments.resolve(resolver))
                    .build()
            ),
            getObservables(locale, arguments)
        );
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
