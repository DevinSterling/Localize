package com.devinsterling.localize.swing;

/// A reactive Swing binding associated with a bound component.
///
/// @param <T> Bound type.
/// @since 2.0
public interface Binding<T> extends Subscription {
    /// Forces the binding to update its [value][get] and bound component.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// This has no effect if [isActive] is `false`.
    void update();

    /// Returns the currently bound value.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// If [#isActive] is `false`, the last value computed while active is returned.
    ///
    /// @return Current value.
    T get();

    /// {@inheritDoc}
    ///
    /// ### Note
    /// A binding may become inactive when its bound component is garbage collected,
    /// even if [dispose] is never explicitly called.
    @Override boolean isActive();
}
