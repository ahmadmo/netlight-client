package org.netlight.util.concurrent;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * @author ahmad
 */
public final class AtomicLongField {

    @SuppressWarnings("unused")
    private volatile long value;
    private final AtomicLongFieldUpdater<AtomicLongField> updater =
            AtomicLongFieldUpdater.newUpdater(AtomicLongField.class, "value");

    public AtomicLongField() {
    }

    public AtomicLongField(long value) {
        updater.set(this, value);
    }

    public long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
        return updater.accumulateAndGet(this, x, accumulatorFunction);
    }

    public long addAndGet(long delta) {
        return updater.addAndGet(this, delta);
    }

    public long decrementAndGet() {
        return updater.decrementAndGet(this);
    }

    public long get() {
        return updater.get(this);
    }

    public long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
        return updater.getAndAccumulate(this, x, accumulatorFunction);
    }

    public long getAndAdd(long delta) {
        return updater.getAndAdd(this, delta);
    }

    public long getAndDecrement() {
        return updater.getAndDecrement(this);
    }

    public long getAndIncrement() {
        return updater.getAndIncrement(this);
    }

    public long getAndSet(long newValue) {
        return updater.getAndSet(this, newValue);
    }

    public long getAndUpdate(LongUnaryOperator updateFunction) {
        return updater.getAndUpdate(this, updateFunction);
    }

    public long incrementAndGet() {
        return updater.incrementAndGet(this);
    }

    public long updateAndGet(LongUnaryOperator updateFunction) {
        return updater.updateAndGet(this, updateFunction);
    }

    public void set(long newValue) {
        updater.set(this, newValue);
    }

    public boolean compareAndSet(long expect, long update) {
        return updater.compareAndSet(this, expect, update);
    }

    public void lazySet(long newValue) {
        updater.lazySet(this, newValue);
    }

    public boolean weakCompareAndSet(long expect, long update) {
        return updater.weakCompareAndSet(this, expect, update);
    }

}
