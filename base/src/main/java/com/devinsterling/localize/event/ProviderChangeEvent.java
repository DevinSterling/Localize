package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;
import com.devinsterling.localize.ResourceBundleProvider;

import java.util.List;

/// An event representing whenever a provider changes in a [Localize] instance.
///
/// ### Events
/// - [Added]
/// - [Replaced]
/// - [Removed] / [BulkRemoved]
/// - [Refreshed] / [BulkRefreshed]
///
/// For each and every event, the return types are **never null**.
///
///  ### Example Usage
/// Inspecting events using pattern matching:
/// ```java
/// @Override protected void onProviderChanged(ProviderChangeEvent event) {
///     super.onProviderChanged(event);
///
///     switch (event) {
///         case ProviderChangeEvent.Added added -> {
///             System.out.println("Added: " + added.getEntry().getKey());
///         }
///         case ProviderChangeEvent.Refreshed refreshed -> {
///             System.out.println("Refreshed: " + refreshed.getEntry().getKey());
///         }
///         default -> System.out.println(event);
///     }
/// }
/// ```
/// @since 2.0
public interface ProviderChangeEvent extends LocalizeEvent {
    /// An event representing that a single entry was added.
    ///
    /// @see Localize#putProvider(Localize.ProviderKey, ResourceBundleProvider)
    /// @see Localize#addProvider(ResourceBundleProvider)
    interface Added extends ProviderChangeEvent {
        /// Returns the added entry.
        ///
        /// @return Added entry.
        Localize.ProviderEntry getEntry();
    }

    /// An event representing that a single entry was replaced.
    ///
    /// @see Localize#putProvider(Localize.ProviderKey, ResourceBundleProvider)
    /// @see Localize.ProviderEntry#remove
    interface Replaced extends ProviderChangeEvent {
        /// Returns the old entry.
        ///
        /// @return Old entry.
        Localize.ProviderEntry getOldEntry();

        /// Returns the new entry.
        ///
        /// @return New entry; the replacement.
        Localize.ProviderEntry getNewEntry();
    }

    /// An event representing that a single entry was removed.
    ///
    /// @see Localize#removeProvider(Localize.ProviderKey)
    /// @see Localize.ProviderEntry#remove
    interface Removed extends ProviderChangeEvent {
        /// Returns the removed entry.
        ///
        /// @return Removed entry.
        Localize.ProviderEntry getEntry();
    }

    /// An event representing that a single entry was refreshed.
    ///
    /// @see Localize#refresh(Localize.ProviderKey)
    /// @see Localize.ProviderEntry#refresh
    interface Refreshed extends ProviderChangeEvent {
        /// Returns the refreshed entry.
        ///
        /// @return Refreshed entry.
        Localize.ProviderEntry getEntry();
    }

    /// A coalesced event representing that more than one entry were removed.
    ///
    /// @see Localize#clearProviders()
    interface BulkRemoved extends ProviderChangeEvent {
        /// Returns the removed entries.
        ///
        /// @return Non-empty collection of removed entries.
        List<Localize.ProviderEntry> getEntries();
    }

    /// A coalesced event representing that more than one entry were refreshed.
    ///
    /// @see Localize#refresh()
    interface BulkRefreshed extends ProviderChangeEvent {
        /// Returns the refreshed entries.
        ///
        /// @return Non-empty collection of refreshed entries.
        List<Localize.ProviderEntry> getEntries();
    }
}
