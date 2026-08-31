package com.devinsterling.localize.swing;

/// A registry that manages reactive Swing bindings.
///
/// @see LocalizeSwing#getBindingRegistry
/// @since 2.0
public interface BindingRegistry {
    /// Registers the given binding, if it isn't already.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// Registering an already registered binding has no effect.
    ///
    /// @param binding Binding to register.
    /// @throws NullPointerException If `binding` is `null`.
    void register(Binding<?> binding);

    /// Unregisters the given binding, if registered.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// @param binding Binding to unregister.
    /// @throws NullPointerException If `binding` is `null`.
    void unregister(Binding<?> binding);
}
