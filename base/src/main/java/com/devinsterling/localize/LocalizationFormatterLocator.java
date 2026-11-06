package com.devinsterling.localize;

import com.devinsterling.localize.spi.LocalizationFormatterProvider;

import java.util.Comparator;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

final class LocalizationFormatterLocator {
    private static final Accessor PROVIDER_ACCESSOR = load();

    private LocalizationFormatterLocator() {}

    private static Accessor load() {
        LocalizationFormatterProvider provider = null;
        Throwable failure = null;

        try {
            provider = ServiceLoader.load(LocalizationFormatterProvider.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .max(
                        Comparator.comparingInt(LocalizationFormatterProvider::getPriority)
                            .thenComparing(p -> p.getClass().getName())
                    )
                    .orElse(() -> LocalizationFormatter.STANDARD);
        } catch (ServiceConfigurationError | RuntimeException e) {
            failure = e;
        }

        return new Accessor(provider, failure);
    }

    static LocalizationFormatter provide() {
        if (PROVIDER_ACCESSOR.failure != null) {
            throw new IllegalStateException(
                "Failed to load a `LocalizationFormatterProvider` via SPI",
                PROVIDER_ACCESSOR.failure
            );
        }

        return Objects.requireNonNull(
            PROVIDER_ACCESSOR.provider.provide(),
            "`LocalizationFormatterProvider.provide()` must not return null; provider: "
                + PROVIDER_ACCESSOR.provider.getClass().getName()
        );
    }

    private record Accessor(LocalizationFormatterProvider provider, Throwable failure) {}
}
