package com.devinsterling.localize.fx;

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

    /// Instantiate a builder to request a specified localized binding.
    ///
    /// @param key     Key to request a formatted localized value for.
    /// @param locale  Observable of the selected locale.
    /// @param applier Callback to apply the properties of this builder
    ///                to the requested value.
    public FXLocalizationValueBuilder(String key, ObservableValue<?> locale, Applier applier) {
        super(key, applier);
        this.locale = locale;
    }

    /// Retrieve an observable formatted string with all properties
    /// applied from this builder.
    ///
    /// The binding is automatically updated when any of the
    /// passed observable arguments or the locale changes.
    ///
    /// @return The observable formatted localized value.
    public StringBinding binding() {
        // Effectively final variables to prevent implicit reference to this class
        String key = getKey();
        Applier applier = getApplier();
        ObservableValue<?> locale = this.locale;

        // Retrieve a snapshot
        Map<String, Object> arguments = Map.copyOf(getArguments());

        return Bindings.createStringBinding(
            () -> applier.evaluate(new LocalizationRequest(key, swapObservables(arguments))),
            getObservables(locale, arguments)
        );
    }

    private static Observable[] getObservables(Observable locale, Map<String, Object> arguments) {
        // Set to avoid duplicate observables
        Set<Observable> observables = new HashSet<>(arguments.size());

        for (Object object : arguments.values()) {
            if (object instanceof Observable observable) {
                observables.add(observable);
            }
        }
        observables.add(locale);

        return observables.toArray(new Observable[0]);
    }

    private static Map<String, Object> swapObservables(Map<String, Object> arguments) {
        Map<String, Object> normalizedMap = new HashMap<>(arguments);

        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (entry.getValue() instanceof ObservableValue<?> observable) {
                normalizedMap.put(entry.getKey(), observable.getValue());
            }
        }

        return normalizedMap;
    }
}
