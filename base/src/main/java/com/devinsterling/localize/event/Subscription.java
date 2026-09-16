package com.devinsterling.localize.event;

import com.devinsterling.localize.Localize;

import java.util.Objects;

/// An event subscription to dispose or cancel an action.
///
/// Thread-safety is implementation specific (e.g., [Localize#addListener] returns thread-safe subscriptions),
/// whereas an integration module (e.g., `localize-swing`)
/// can return subscriptions constrained to a single thread only.
///
/// @since 2.0
public interface Subscription {
    /// An empty subscription that is always inactive and does nothing when disposed.
    Subscription EMPTY = new Subscription() {
        @Override public void dispose() {
            // no-op
        }

        @Override public boolean isActive() {
            return false;
        }
    };

    /// Disposes the subscription, or does nothing if already Disposed.
    ///
    /// Subsequent calls have no effect.
    void dispose();

    /// Returns `true` if the subscription is currently active.
    ///
    /// @return `true` if the subscription is active, or `false` if not disposed.
    boolean isActive();

    /// Returns a new [Subscription] with this and another subscription combined into one.
    ///
    /// This method is equivalent to [`Subscription.combine(this, other)`][combine].
    ///
    /// @param other Subscription to combine with.
    /// @return Combined subscription.
    /// @throws NullPointerException If `other` is `null`.
    default Subscription and(Subscription other) {
        Objects.requireNonNull(other, "other subscription must not be null");
        Subscription current = this;

        return new Subscription() {
            @Override public void dispose() {
                current.dispose();
                other.dispose();
            }

            @Override public boolean isActive() {
                return current.isActive() || other.isActive();
            }
        };
    }

    /// Returns a [Subscription] that has all given subscriptions combined into one.
    ///
    /// If the resulting combined subscription is disposed, then all given subscriptions are disposed.
    ///
    /// @param subscriptions Subscriptions to combine.
    /// @return Combined subscription.
    /// @throws NullPointerException If `subscriptions` or any array entry is `null`.
    static Subscription combine(Subscription... subscriptions) {
        Objects.requireNonNull(subscriptions, "subscriptions must not be null");
        if (subscriptions.length == 0) return EMPTY;

        for (Subscription subscription : subscriptions) {
            Objects.requireNonNull(subscription, "subscription entries must not be null");
        }

        return new Subscription() {
            @Override public void dispose() {
                for (Subscription subscription : subscriptions) {
                    subscription.dispose();
                }
            }

            @Override public boolean isActive() {
                for (Subscription subscription : subscriptions) {
                    if (subscription.isActive()) {
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
