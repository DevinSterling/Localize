package com.devinsterling.localize.spi;

import com.devinsterling.localize.LocalizationFormatter;

import java.util.ServiceLoader;

public class LocalizationFormatterLocator {
    public static final LocalizationFormatter DEFAULT = loadFormatter();

    private static LocalizationFormatter loadFormatter() {
        ServiceLoader<LocalizationFormatter> loader = ServiceLoader.load(LocalizationFormatter.class);

        return loader.findFirst().orElse(LocalizationFormatter.DEFAULT);
    }
}
