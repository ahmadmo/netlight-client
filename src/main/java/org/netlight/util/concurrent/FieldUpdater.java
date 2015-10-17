package org.netlight.util.concurrent;

import org.netlight.util.TimeProperty;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

/**
 * @author ahmad
 */
public final class FieldUpdater<V> {

    private volatile V value;
    private final Function<V, V> updater;
    private final AtomicLong updateInterval;
    private final AtomicLong lastSet;
    private final AtomicReferenceFieldUpdater<FieldUpdater, Object> valueUpdater;

    public FieldUpdater(V value, Function<V, V> updater, TimeProperty updateInterval) {
        this.value = value;
        this.updater = updater;
        this.updateInterval = new AtomicLong(updateInterval.to(TimeUnit.MILLISECONDS));
        lastSet = new AtomicLong(System.currentTimeMillis());
        valueUpdater = AtomicReferenceFieldUpdater.newUpdater(FieldUpdater.class, Object.class, "value");
    }

    public V get() {
        long n = (System.currentTimeMillis() - lastSet.get()) / updateInterval.get();
        if (n > 0) {
            final V e = value;
            V u = e;
            for (; n > 0; n--) {
                u = updater.apply(u);
            }
            compareAndSet0(e, u);
        }
        return value;
    }

    private boolean compareAndSet0(V expect, V update) {
        boolean set = valueUpdater.compareAndSet(this, expect, update);
        if (set) {
            lastSet.set(System.currentTimeMillis());
        }
        return set;
    }

    public void set(V value) {
        valueUpdater.set(this, value);
        lastSet.set(System.currentTimeMillis());
    }

    public boolean compareAndSet(V expect, V update) {
        get();
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
