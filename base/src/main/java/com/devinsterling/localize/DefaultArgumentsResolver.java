package com.devinsterling.localize;

import java.util.function.Supplier;

/// The default arguments resolver for [LocalizationValueBuilder].
///
/// Objects not specified in the table below are retained as-is.
/// Otherwise, they are resolved as:
///
/// | Object     | Resolution Method |
/// |------------|-------------------|
/// | [Supplier] | [Supplier#get]    |
///
/// @see LocalizationValueBuilder#getResolver
/// @since 2.0
public class DefaultArgumentsResolver implements Arguments.Resolver {
    static final DefaultArgumentsResolver INSTANCE = new DefaultArgumentsResolver();

    @Override public Object resolve(Object value) {
        if (value instanceof Supplier<?> supplier) {
            value = supplier.get();
        }

        return value;
    }
}
