package com.devinsterling.localize;

import java.util.function.Supplier;

public class DefaultArgumentsResolver implements Arguments.Resolver {
    static final DefaultArgumentsResolver INSTANCE = new DefaultArgumentsResolver();

    @Override public Object resolve(Object value) {
        if (value instanceof Supplier<?> supplier) {
            value = supplier.get();
        }

        return value;
    }
}
