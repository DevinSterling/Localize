package com.devinsterling.localize.swing;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

final class WeakSupplier<T, U> implements Supplier<U> {
    private final WeakReference<T> reference;
    private final Function<T, U> supplier;

    public WeakSupplier(T value, Function<T, U> supplier) {
        this.reference = new WeakReference<>(Objects.requireNonNull(value, "value must not be null"));
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    @Override public U get() {
        T value = reference.get();
        return value == null ? null : supplier.apply(value);
    }
}
