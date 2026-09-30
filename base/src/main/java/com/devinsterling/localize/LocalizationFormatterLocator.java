package com.devinsterling.localize;

import com.devinsterling.localize.spi.LocalizationFormatterProvider;

import java.util.Comparator;
import java.util.ServiceLoader;

final class LocalizationFormatterLocator {
    static final LocalizationFormatterProvider PROVIDER = loadProvider();

    private LocalizationFormatterLocator() {}

    private static LocalizationFormatterProvider loadProvider() {
        return ServiceLoader.load(LocalizationFormatterProvider.class)
                            .stream()
                            .map(ServiceLoader.Provider::get)
                            .max(Comparator.comparingInt(LocalizationFormatterProvider::getPriority))
                            .orElse(() -> LocalizationFormatter.STANDARD);
    }
}
