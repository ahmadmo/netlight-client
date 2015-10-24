package org.netlight.util.concurrent;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * @author ahmad
 */
public final class AtomicIntegerField {

    @SuppressWarnings("unused")
    private volatile int value;
    private final AtomicIntegerFieldUpdater<AtomicIntegerField> updater =
            AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerField.class, "value");

    public AtomicIntegerField() {
    }

    public AtomicIntegerField(int value) {
        updater.set(this, value);
    }

    public int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
        return updater.accumulateAndGet(this, x, accumulatorFunction);
    }

    public int addAndGet(int delta) {
        return updater.addAndGet(this, delta);
    }

    public int decrementAndGet() {
        return updater.decrementAndGet(this);
    }

    public int get() {
        return updater.get(this);
    }

    public int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
        return updater.getAndAccumulate(this, x, accumulatorFunction);
    }

    public int getAndAdd(int delta) {
        return updater.getAndAdd(this, delta);
    }

    public int getAndDecrement() {
        return updater.getAndDecrement(this);
    }

    public int getAndIncrement() {
        return updater.getAndIncrement(this);
    }

    public int getAndSet(int newValue) {
        return updater.getAndSet(this, newValue);
    }

    public int getAndUpdate(IntUnaryOperator updateFunction) {
        return updater.getAndUpdate(this, updateFunction);
    }

    public int incrementAndGet() {
        return updater.incrementAndGet(this);
    }

    public int updateAndGet(IntUnaryOperator updateFunction) {
        return updater.updateAndGet(this, updateFunction);
    }

    public void set(int newValue) {
        updater.set(this, newValue);
    }

    public boolean compareAndSet(int expect, int update) {
        return updater.compareAndSet(this, expect, update);
    }

    public void lazySet(int newValue) {
        updater.lazySet(this, newValue);
    }

    public boolean weakCompareAndSet(int expect, int update) {
        return updater.weakCompareAndSet(this, expect, update);
    }

}
