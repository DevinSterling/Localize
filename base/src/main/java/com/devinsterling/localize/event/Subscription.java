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

    /// Disposes the subscription, or does nothing if already disposed.
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
    /// If disposing either subscription throws a [RuntimeException], the other subscription
    /// is still given an opportunity to be disposed. If both throw, the runtime exceptions
    /// are consolidated, with the second exception added as a suppressed exception.
    ///
    /// @param other Subscription to combine with.
    /// @return Combined subscription.
    /// @throws NullPointerException If `other` is `null`.
    default Subscription and(Subscription other) {
        record And(Subscription a, Subscription b) implements Subscription {
            @Override public void dispose() {
                disposeAll(a, b);
            }

            @Override public boolean isActive() {
                return a.isActive() || b.isActive();
            }
        }

        Objects.requireNonNull(other, "other subscription must not be null");
        return new And(this, other);
    }

    /// Returns a [Subscription] that has all given subscriptions combined into one.
    ///
    /// If the resulting combined subscription is disposed, then all given subscriptions are disposed.
    ///
    /// If disposing any subscription throws a [RuntimeException], the other subscriptions
    /// are still given an opportunity to be disposed. If more than one throws, the runtime
    /// exceptions are consolidated, with subsequent added as suppressed exceptions.
    ///
    /// @param subscriptions Subscriptions to combine.
    /// @return Combined subscription.
    /// @throws NullPointerException If `subscriptions` or any array entry is `null`.
    static Subscription combine(Subscription... subscriptions) {
        record Combine(Subscription[] subscriptions) implements Subscription {
            @Override public void dispose() {
                disposeAll(subscriptions);
            }

            @Override public boolean isActive() {
                for (Subscription subscription : subscriptions) {
                    if (subscription.isActive()) {
                        return true;
                    }
                }

                return false;
            }
        }

        Objects.requireNonNull(subscriptions, "subscriptions must not be null");
        if (subscriptions.length == 0) return EMPTY;

        for (Subscription subscription : subscriptions) {
            Objects.requireNonNull(subscription, "subscription entries must not be null");
        }

        return new Combine(subscriptions);
    }

    private static void disposeAll(Subscription... subscriptions) {
        RuntimeException caught = null;

        for (Subscription sub : subscriptions) {
            try {
                sub.dispose();
            } catch (RuntimeException t) {
                if (caught == null) {
                    caught = t;
                } else {
                    caught.addSuppressed(t);
                }
            }
        }

        if (caught != null) {
            throw caught;
        }
    }
}
