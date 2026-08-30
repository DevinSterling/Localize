package com.devinsterling.localize.icu4j;

import com.devinsterling.localize.LocalizationFormatter;

import com.ibm.icu.message2.MessageFormatter;
import com.ibm.icu.text.MessageFormat;

interface IcuMessageFormatStrategy {
    String format(LocalizationFormatter.Request request);

    IcuMessageFormatStrategy FORMAT1 = request -> {
        if (request.getArguments().isEmpty()) return request.getPattern();

        MessageFormat formatter = new MessageFormat(request.getPattern(), request.getLocale());

        // Prefer named arguments over positional (see IcuFormater#argumentsHint)
        return formatter.format(request.getArguments().toNamedMap());
    };

    IcuMessageFormatStrategy FORMAT2 = request -> {
        MessageFormatter formatter = MessageFormatter.builder()
                .setLocale(request.getLocale())
                .setPattern(request.getPattern())
                .build();

        return formatter.formatToString(request.getArguments().toNamedMap());
    };
}
