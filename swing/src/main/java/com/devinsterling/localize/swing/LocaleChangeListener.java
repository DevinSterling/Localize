package com.devinsterling.localize.swing;

import java.util.Locale;

/// A listener that is notified whenever the locale changes.
///
/// @see LocalizeSwing#addLocaleListener
/// @see LocalizeSwing#removeLocaleListener
/// @since 2.0
public interface LocaleChangeListener {
    /// Notifies that the locale has changed.
    ///
    /// This method is called whenever the locale changes.
    ///
    /// @param oldLocale The previously set locale.
    /// @param newLocale The newly set locale.
    void onChange(Locale oldLocale, Locale newLocale);
}
