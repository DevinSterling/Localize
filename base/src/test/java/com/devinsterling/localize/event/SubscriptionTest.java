package com.devinsterling.localize.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionTest {

    @Test void testEmptySubscription() {
        Subscription subscription = Subscription.EMPTY;

        // Always false for an `EMPTY` subscription.
        assertFalse(subscription.isActive());

        // dispose is a no-op
        subscription.dispose();
        assertFalse(subscription.isActive());
    }

    @Test void testSubscriptionAnd() {
        Subscription a = createSubscription();
        Subscription b = createSubscription();
        Subscription ab = a.and(b);

        assertTrue(ab.isActive());

        b.dispose();
        assertTrue(ab.isActive());

        a.dispose();
        assertFalse(ab.isActive());

        Subscription c = createSubscription();
        Subscription d = createSubscription();
        Subscription cd = c.and(d);
        Subscription cd2 = c.and(d);

        cd.dispose();
        assertFalse(cd.isActive());
        assertFalse(cd2.isActive());
        assertFalse(c.isActive());
        assertFalse(d.isActive());

    }

    @Test void testSubscriptionCombine() {
        Subscription a = Subscription.EMPTY;
        assertEquals(a, Subscription.combine());

        Subscription b = createSubscription();
        Subscription ab = Subscription.combine(a, b);
        assertTrue(ab.isActive());

        b.dispose();
        assertFalse(ab.isActive());

        Subscription c = createSubscription();
        Subscription d = createSubscription();
        Subscription cd = Subscription.combine(c, d);
        Subscription cd2 = Subscription.combine(c, d);

        cd.dispose();
        assertFalse(cd.isActive());
        assertFalse(cd2.isActive());
        assertFalse(c.isActive());
        assertFalse(d.isActive());
    }

    @Test void testCompositeDisposal() {
        record ExceptionSubscription() implements Subscription {
            @Override public void dispose() {
                throw new RuntimeException();
            }

            @Override public boolean isActive() {
                return false;
            }
        }

        Subscription a = new ExceptionSubscription();
        Subscription b = createSubscription();
        Subscription ab = a.and(b);

        assertTrue(ab.isActive());
        assertThrows(RuntimeException.class, ab::dispose);
        assertFalse(ab.isActive());

        Subscription c = createSubscription();
        Subscription e = createSubscription();
        Subscription acae = Subscription.combine(a, c, a, e);

        assertTrue(acae.isActive());
        assertThrows(RuntimeException.class, acae::dispose);
        assertFalse(acae.isActive());
    }

    @Test void testCompositeDisposalExceptionInspection() {
        record ExceptionSubscription(RuntimeException exception) implements Subscription {
            @Override public void dispose() {
                throw exception;
            }

            @Override public boolean isActive() {
                return false;
            }
        }

        RuntimeException exceptionA = new RuntimeException();
        RuntimeException exceptionB = new RuntimeException();
        RuntimeException caught;

        Subscription a = new ExceptionSubscription(exceptionA);
        Subscription b = createSubscription();
        Subscription ab = a.and(b);

        caught = assertThrows(RuntimeException.class, ab::dispose);
        assertSame(exceptionA, caught);
        assertEquals(0, caught.getSuppressed().length);

        Subscription c = createSubscription();
        Subscription d = new ExceptionSubscription(exceptionB);
        Subscription e = createSubscription();
        Subscription dcae = Subscription.combine(d, c, a, e);

        caught = assertThrows(RuntimeException.class, dcae::dispose);
        assertSame(exceptionB, caught);
        assertEquals(1, caught.getSuppressed().length);
        assertSame(exceptionA, caught.getSuppressed()[0]);
    }

    @Test void testSubscriptionAndCombineNonNull() {
        assertThrows(NullPointerException.class, () -> Subscription.EMPTY.and(null));
        assertThrows(NullPointerException.class, () -> Subscription.combine((Subscription[]) null));
        assertThrows(NullPointerException.class, () -> Subscription.combine(Subscription.EMPTY, null));
    }

    private static Subscription createSubscription() {
        return new Subscription() {
            boolean isActive = true;

            @Override public void dispose() {
                isActive = false;
            }

            @Override public boolean isActive() {
                return isActive;
            }
        };
    }
}

