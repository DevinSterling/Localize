package com.devinsterling.localize.swing;

import java.util.Objects;

/// A reactive Swing subscription.
///
/// @since 2.0
public interface Subscription {
    /// An empty subscription that is always inactive.
    Subscription EMPTY = new Subscription() {
        @Override public void dispose() {
            // no-op
        }

        @Override public boolean isActive() {
            return false;
        }
    };

    /// Dispose a subscription.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// Subsequent calls have no effect.
    void dispose();

    /// Returns `true` if the subscription is currently active.
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// @return `true` if the subscription is active; not disposed.
    boolean isActive();

    /// Creates a subscription that invokes the given callback once on the first call to [dispose].
    ///
    /// Subsequent calls to [dispose] will have no effect after the first call.
    ///
    /// @param onDispose Callback to invoke on the first call to [dispose].
    /// @throws NullPointerException If `onDispose` is `null`.
    static Subscription of(Runnable onDispose) {
        Objects.requireNonNull(onDispose, "onDispose must not be null");

        return new Subscription() {
            private boolean isActive = true;

            @Override public void dispose() {
                if (isActive) {
                    isActive = false;
                    onDispose.run();
                }
            }

            @Override public boolean isActive() {
                return isActive;
            }
        };
    }
}
