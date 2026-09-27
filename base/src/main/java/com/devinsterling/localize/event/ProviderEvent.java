package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.LocalizeConfig;
import com.devinsterling.localize.ResourceBundleProvider;

import java.util.List;
import java.util.Locale;

/// An [event][LocalizeEvent] associated with one or more [ResourceBundleProvider]s.
///
/// ### Events
/// - [Added]
/// - [Replaced]
/// - [Removed] / [BulkRemoved]
/// - [Refreshed] / [BulkRefreshed]
/// - [ExceptionCaught]
///
/// For each and every event, the return types are **never null**.
///
///  ### Example Usage
/// Inspecting events using pattern matching:
/// ```java
/// @Override protected void onProviderChanged(ProviderEvent event) {
///     super.onProviderChanged(event);
///
///     switch (event) {
///         case ProviderEvent.Added added -> {
///             System.out.println("Added: " + added.getEntry().getKey());
///         }
///         case ProviderEvent.Refreshed refreshed -> {
///             System.out.println("Refreshed: " + refreshed.getEntry().getKey());
///         }
///         default -> System.out.println(event);
///     }
/// }
/// ```
/// @since 2.0
public interface ProviderEvent extends LocalizeEvent {
    /// An event indicating that a single entry was added.
    ///
    /// @see Localize#putProvider(Localize.ProviderKey, ResourceBundleProvider)
    /// @see Localize#addProvider(ResourceBundleProvider)
    interface Added extends ProviderEvent {
        /// Returns the added entry.
        ///
        /// @return Added entry.
        Localize.ProviderEntry getEntry();
    }

    /// An event indicating that a single entry was replaced.
    ///
    /// @see Localize#putProvider(Localize.ProviderKey, ResourceBundleProvider)
    /// @see Localize.ProviderEntry#remove
    interface Replaced extends ProviderEvent {
        /// Returns the old entry.
        ///
        /// @return Old entry.
        Localize.ProviderEntry getOldEntry();

        /// Returns the new entry.
        ///
        /// @return New entry; the replacement.
        Localize.ProviderEntry getNewEntry();
    }

    /// An event indicating that a single entry was removed.
    ///
    /// @see Localize#removeProvider(Localize.ProviderKey)
    /// @see Localize.ProviderEntry#remove
    interface Removed extends ProviderEvent {
        /// Returns the removed entry.
        ///
        /// @return Removed entry.
        Localize.ProviderEntry getEntry();
    }

    /// An event indicating that a single entry was refreshed.
    ///
    /// @see Localize#refreshProvider(Localize.ProviderKey)
    /// @see Localize.ProviderEntry#refresh
    interface Refreshed extends ProviderEvent {
        /// Returns the refreshed entry.
        ///
        /// @return Refreshed entry.
        Localize.ProviderEntry getEntry();
    }

    /// A coalesced event indicating that more than one entry were removed.
    ///
    /// @see Localize#clearProviders
    interface BulkRemoved extends ProviderEvent {
        /// Returns the removed entries.
        ///
        /// @return Non-empty collection of removed entries.
        List<Localize.ProviderEntry> getEntries();
    }

    /// A coalesced event indicating that more than one entry were refreshed.
    ///
    /// @see Localize#refreshProviders()
    interface BulkRefreshed extends ProviderEvent {
        /// Returns the refreshed entries.
        ///
        /// @return Non-empty collection of refreshed entries.
        List<Localize.ProviderEntry> getEntries();
    }

    /// An event indicating that an unexpected [Exception] was
    /// [caught][getException] while calling [ResourceBundleProvider#getBundle].
    ///
    /// This event is propagated regardless of whether
    /// [LocalizeConfig#isIgnoreProviderExceptions()] is toggled or not.
    interface ExceptionCaught extends ProviderEvent, DiagnosticEvent {
        /// Returns the provider entry where the [unexpected exception][getException] occurred from.
        ///
        /// @return Source provider entry.
        /// @see getProvider
        Localize.ProviderEntry getEntry();

        /// Returns the provider where the [unexpected exception][getException] occurred from.
        ///
        /// @return Source provider.
        default ResourceBundleProvider getProvider() {
            return getEntry().getProvider();
        }

        /// Returns the locale passed to [ResourceBundleProvider#getBundle].
        ///
        /// @return Source locale.
        Locale getLocale();

        /// Returns the caught exception.
        ///
        /// @return Caught exception.
        Exception getException();
    }
}
