package org.netlight;

import org.netlight.util.TimeProperty;
import org.netlight.util.concurrent.AtomicBooleanField;
import org.netlight.util.concurrent.AtomicLongField;
import org.netlight.util.concurrent.AtomicReferenceField;
import org.netlight.util.concurrent.PeriodicAtomicReferenceFieldUpdater;

/**
 * @author ahmad
 */
public final class AtomicFieldsTest {

    public static void main(String[] args) throws InterruptedException {
        AtomicBooleanField b = new AtomicBooleanField();
        assertTrue(!b.get());
        assertTrue(!b.getAndSet(true));
        assertTrue(b.compareAndSet(true, false));
        AtomicLongField l = new AtomicLongField();
        assertEquals(0L, l.get());
        assertEquals(0L, l.getAndSet(2));
        assertTrue(l.compareAndSet(2, 3));
        AtomicReferenceField<String> s = new AtomicReferenceField<>("hello");
        assertEquals(s.get(), "hello");
        assertEquals(s.getAndSet("hello, world!"), "hello");
        assertTrue(s.compareAndSet("hello, world!", "hello"));
        PeriodicAtomicReferenceFieldUpdater<Integer> periodicUpdater
                = new PeriodicAtomicReferenceFieldUpdater<>(0, i -> i + 1, TimeProperty.seconds(1));
        assertEquals(0, periodicUpdater.get());
        Thread.sleep(1000);
        assertEquals(1, periodicUpdater.get());
        assertTrue(periodicUpdater.compareAndSet(1, 2));
        Thread.sleep(1000);
        assertEquals(3, periodicUpdater.get());
    }

    static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("true");
        }
    }

    static void assertEquals(Object o1, Object o2) {
        if (!o1.equals(o2)) {
            throw new AssertionError(o1 + " != " + o2);
        }
    }

}
