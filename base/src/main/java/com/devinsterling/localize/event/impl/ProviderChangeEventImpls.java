package com.devinsterling.localize.event.impl;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.event.ProviderChangeEvent;

import java.util.Collections;
import java.util.List;

public final class ProviderChangeEventImpls {

    private ProviderChangeEventImpls() {}

    public record Added(
        Localize getSource, 
        Localize.ProviderEntry getEntry
    ) implements ProviderChangeEvent.Added {}

    public record Replaced(
        Localize getSource, 
        Localize.ProviderEntry getOldEntry,
        Localize.ProviderEntry getNewEntry
    ) implements ProviderChangeEvent.Replaced {}

    public record Removed(
        Localize getSource, 
        Localize.ProviderEntry getEntry
    ) implements ProviderChangeEvent.Removed {}

    public record Refreshed(
        Localize getSource, 
        Cause getCause, 
        Localize.ProviderEntry getEntry
    ) implements ProviderChangeEvent.Refreshed {
        public Refreshed(Localize source, Localize.ProviderEntry entry) {
            this(source, Cause.EXTERNAL, entry);
        }
    }

    public record BulkRemoved(
        Localize getSource, 
        List<Localize.ProviderEntry> getEntries
    ) implements ProviderChangeEvent.BulkRemoved {
        public BulkRemoved {
            getEntries = Collections.unmodifiableList(getEntries);
        }
    }

    public record BulkRefreshed(
        Localize getSource,
        Cause getCause,
        List<Localize.ProviderEntry> getEntries
    ) implements ProviderChangeEvent.BulkRefreshed {
        public BulkRefreshed {
            getEntries = Collections.unmodifiableList(getEntries);
        }
    }
}
