package org.netlight.util.concurrent;

/**
 * @author ahmad
 */
public final class AtomicBooleanField {

    private final AtomicIntegerField updater = new AtomicIntegerField();

    public AtomicBooleanField() {
    }

    public AtomicBooleanField(boolean value) {
        updater.set(intValue(value));
    }

    public boolean get() {
        return updater.get() == 1;
    }

    public void set(boolean newValue) {
        updater.set(intValue(newValue));
    }

    public boolean compareAndSet(boolean expect, boolean update) {
        return updater.compareAndSet(intValue(expect), intValue(update));
    }

    public boolean getAndSet(boolean newValue) {
        return updater.getAndSet(intValue(newValue)) == 1;
    }

    public void lazySet(boolean newValue) {
        updater.lazySet(intValue(newValue));
    }

    public boolean weakCompareAndSet(boolean expect, boolean update) {
        return updater.weakCompareAndSet(intValue(expect), intValue(update));
    }

    private static int intValue(boolean value) {
        return value ? 1 : 0;
    }

}
