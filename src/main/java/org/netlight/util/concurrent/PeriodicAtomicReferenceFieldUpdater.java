package org.netlight.util.concurrent;

import org.netlight.util.TimeProperty;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author ahmad
 */
public final class PeriodicAtomicReferenceFieldUpdater<V> {

    private final AtomicReferenceField<V> value;
    private final Function<V, V> updaterFunction;
    private final AtomicLongField updateInterval;
    private final AtomicLongField lastSet;

    public PeriodicAtomicReferenceFieldUpdater(V value, Function<V, V> updaterFunction, TimeProperty updateInterval) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(updaterFunction);
        Objects.requireNonNull(updateInterval);
        this.value = new AtomicReferenceField<>(value);
        this.updaterFunction = updaterFunction;
        this.updateInterval = new AtomicLongField(updateInterval.to(TimeUnit.MILLISECONDS));
        lastSet = new AtomicLongField(System.currentTimeMillis());
    }

    public V get() {
        update();
        return value.get();
    }

    private void update() {
        long n = (System.currentTimeMillis() - lastSet.get()) / updateInterval.get();
        if (n > 0) {
            final V e = value.get();
            V u = e;
            for (; n > 0; n--) {
                u = updaterFunction.apply(u);
            }
            compareAndSet0(e, u);
        }
    }

    private boolean compareAndSet0(V expect, V update) {
        boolean modified = value.compareAndSet(expect, update);
        if (modified) {
            lastSet.set(System.currentTimeMillis());
        }
        return modified;
    }

    public void set(V value) {
        this.value.set(value);
        lastSet.set(System.currentTimeMillis());
    }

    public boolean compareAndSet(V expect, V update) {
        update();
        return compareAndSet0(expect, update);
    }

    public V getAndSet(V value) {
        V v = get();
        set(value);
        return v;
    }

    public void setUpdateInterval(TimeProperty interval) {
        updateInterval.set(interval.to(TimeUnit.MILLISECONDS));
    }

    public TimeProperty getUpdateInterval() {
        return new TimeProperty(updateInterval.get(), TimeUnit.MICROSECONDS);
    }

}
