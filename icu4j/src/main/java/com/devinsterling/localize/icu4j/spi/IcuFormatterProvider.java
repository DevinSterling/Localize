package com.devinsterling.localize.icu4j.spi;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.icu4j.IcuFormatter;
import com.devinsterling.localize.spi.LocalizationFormatterProvider;

public class IcuFormatterProvider implements LocalizationFormatterProvider {
    @Override public LocalizationFormatter provide() {
        return new IcuFormatter();
    }
}
