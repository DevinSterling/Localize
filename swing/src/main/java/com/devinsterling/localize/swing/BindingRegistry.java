package com.devinsterling.localize.swing;

/// A registry that manages reactive Swing bindings.
///
/// ### Dispatch
/// If a [Binding] throws a [RuntimeException], dispatch continues for subsequent bindings.
/// The first exception is rethrown after dispatch, with any subsequent added as suppressed exceptions.
/// This prevents a single faulty binding from blocking all other bindings.
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
