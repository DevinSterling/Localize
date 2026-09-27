package com.devinsterling.localize.event.impl;

import com.devinsterling.localize.LocalizationFormatter;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.Localize;
import com.devinsterling.localize.event.DiagnosticEvent;
import com.devinsterling.localize.event.EventListener;
import com.devinsterling.localize.event.FormatterEvent;
import com.devinsterling.localize.event.LocalizeEvent;
import com.devinsterling.localize.event.ProviderEvent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class EventImpls {

    private EventImpls() {}

    public interface Diagnostic {
        record MissingKey(
            Localize getSource,
            LocalizationRequestSource.Key getKey,
            LocalizationRequest getRequest
        ) implements DiagnosticEvent.MissingKey {}

        record ListenerExceptionCaught(
            Localize getSource,
            EventListener<?> getListener,
            LocalizeEvent getEvent,
            Exception getException
        ) implements DiagnosticEvent.ListenerExceptionCaught {}
    }

    public interface Formatter {
        record Replaced(
            Localize getSource,
            LocalizationFormatter getOldFormatter,
            LocalizationFormatter getNewFormatter
        ) implements FormatterEvent.Replaced {}

        record ExceptionCaught(
            Localize getSource,
            LocalizationFormatter getFormatter,
            LocalizationFormatter.Request getRequest,
            Exception getException
        ) implements FormatterEvent.ExceptionCaught {}
    }

    public interface Provider {
        record Added(
            Localize getSource,
            Localize.ProviderEntry getEntry
        ) implements ProviderEvent.Added {}

        record Replaced(
            Localize getSource,
            Localize.ProviderEntry getOldEntry,
            Localize.ProviderEntry getNewEntry
        ) implements ProviderEvent.Replaced {}

        record Removed(
            Localize getSource,
            Localize.ProviderEntry getEntry
        ) implements ProviderEvent.Removed {}

        record Refreshed(
            Localize getSource,
            Cause getCause,
            Localize.ProviderEntry getEntry
        ) implements ProviderEvent.Refreshed {
            public Refreshed(Localize source, Localize.ProviderEntry entry) {
                this(source, Cause.DIRECT, entry);
            }
        }

        record BulkRemoved(
            Localize getSource,
            List<Localize.ProviderEntry> getEntries
        ) implements ProviderEvent.BulkRemoved {
            public BulkRemoved {
                getEntries = Collections.unmodifiableList(getEntries);
            }
        }

        record BulkRefreshed(
            Localize getSource,
            Cause getCause,
            List<Localize.ProviderEntry> getEntries
        ) implements ProviderEvent.BulkRefreshed {
            public BulkRefreshed {
                getEntries = Collections.unmodifiableList(getEntries);
            }
        }

        record ExceptionCaught(
            Localize getSource,
            Cause getCause,
            Locale getLocale,
            Exception getException,
            Localize.ProviderEntry getEntry
        ) implements ProviderEvent.ExceptionCaught {}
    }
}
