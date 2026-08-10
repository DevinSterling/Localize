package com.devinsterling.localize.fx;

import com.devinsterling.localize.DefaultArgumentsResolver;

import javafx.beans.value.ObservableValue;

/// ObservableValue are swapped with the value contained within them.
///
/// @since 2.0
public class FXArgumentsResolver extends DefaultArgumentsResolver {
    static final FXArgumentsResolver INSTANCE = new FXArgumentsResolver();

    @Override public Object resolve(Object value) {
        value = super.resolve(value);

        if (value instanceof ObservableValue<?> observable) {
            value = observable.getValue();
        }

        return value;
    }
}
