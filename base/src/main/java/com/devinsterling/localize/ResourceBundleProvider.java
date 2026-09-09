package com.devinsterling.localize;

import java.util.Locale;
import java.util.ResourceBundle;

/// Provider that produces a [ResourceBundle] on demand with a given [Locale].
///
/// @see Localize#putBundleProvider(String, ResourceBundleProvider)
/// @since 1.0
@FunctionalInterface
public interface ResourceBundleProvider {
    /// Retrieves the [ResourceBundle] associated with the given [Locale].
    ///
    /// @param locale Locale for the produced resource bundle to be based upon.
    /// @return       The produced resource bundle.
    ResourceBundle getBundle(Locale locale);
}
