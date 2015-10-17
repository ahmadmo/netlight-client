package org.netlight.util.concurrent;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author ahmad
 */
public final class AtomicReferenceField<V> {

    @SuppressWarnings("all")
    private volatile V value;
    private final AtomicReferenceFieldUpdater<AtomicReferenceField, Object> updater =
            AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceField.class, Object.class, "value");

    public AtomicReferenceField() {
    }

    public AtomicReferenceField(V value) {
        updater.set(this, value);
    }

    @SuppressWarnings("unchecked")
    public V get() {
        return (V) updater.get(this);
    }

    public void set(V newValue) {
        updater.set(this, newValue);
    }

    public boolean compareAndSet(V expect, V update) {
        return updater.compareAndSet(this, expect, update);
    }

    @SuppressWarnings("unchecked")
    public V getAndSet(V newValue) {
        return (V) updater.getAndSet(this, newValue);
    }

}
