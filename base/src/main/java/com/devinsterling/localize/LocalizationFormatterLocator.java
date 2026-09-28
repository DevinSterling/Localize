package com.devinsterling.localize;

import com.devinsterling.localize.spi.LocalizationFormatterProvider;

import java.util.ServiceLoader;

final class LocalizationFormatterLocator {
    static final LocalizationFormatterProvider PROVIDER = loadProvider();

    private LocalizationFormatterLocator() {}

    private static LocalizationFormatterProvider loadProvider() {
        ServiceLoader<LocalizationFormatterProvider> loader = ServiceLoader.load(LocalizationFormatterProvider.class);

        return loader.findFirst().orElse(() -> LocalizationFormatter.STANDARD);
    }
}
