package com.devinsterling.localize;

import java.util.Locale;

/// Provider that produces a [PatternSource] on demand with a given [Locale].
///
/// @implSpec Implementations must be thread-safe for concurrent access.
/// @see PatternSource#of
/// @see Localize#putProvider
/// @since 2.0
@FunctionalInterface
public interface PatternSourceProvider {
    /// Provides the [PatternSource] associated with the given [Locale].
    ///
    /// @param locale Locale for the provided pattern source to be based upon.
    /// @return       Associated pattern source, or `null` if not available.
    PatternSource provide(Locale locale);
}
