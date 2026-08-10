package com.devinsterling.localize.spi;

import com.devinsterling.localize.LocalizationRequestProcessor;

import java.util.ServiceLoader;

public class RequestProcessorRegistry {
    public static final LocalizationRequestProcessor DEFAULT = loadProcessor();

    private static LocalizationRequestProcessor loadProcessor() {
        ServiceLoader<LocalizationRequestProcessor> loader = ServiceLoader.load(LocalizationRequestProcessor.class);

        return loader.findFirst().orElse(LocalizationRequestProcessor.DEFAULT);
    }
}
