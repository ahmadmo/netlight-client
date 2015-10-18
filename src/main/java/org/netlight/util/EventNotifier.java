package org.netlight.util;

import org.netlight.util.concurrent.AtomicBooleanField;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author ahmad
 */
public final class EventNotifier<E, L> {

    private static final Object STOP_EVENT = new Object();

    private final BlockingQueue<Object> events = new LinkedBlockingQueue<>();
    private final List<L> listeners = new CopyOnWriteArrayList<>();
    private final EventNotifierHandler<E, L> handler;
    private final Class<E> eventType;
    private final AtomicBooleanField running = new AtomicBooleanField();

    public EventNotifier(EventNotifierHandler<E, L> handler,Class<E> eventType) {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(eventType);
        this.handler = handler;
        this.eventType = eventType;
    }

    public void notify(E event) {
        events.offer(event);
    }

    public void addListener(L listener) {
        listeners.add(listener);
    }

    public void removeListener(L listener) {
        listeners.remove(listener);
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            Thread t = new Thread(() -> {
                boolean exceptional = false;
                try {
                    while (!Thread.currentThread().isInterrupted() && running.get()) {
                        Object o = next();
                        if (o != null) {
                            if (o == STOP_EVENT) {
                                break;
                            } else if (eventType.isInstance(o)) {
                                for (L l : listeners) {
                                    @SuppressWarnings("unchecked")
                                    E e = (E) o;
                                    handler.handle(e, l);
                                }
                            }
                        }
                    }
                } catch (Throwable cause) {
                    exceptional = true;
                    running.set(false);
                    handler.exceptionCaught(cause);
                } finally {
                    if (!exceptional) {
                        running.set(false);
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private Object next() {
        try {
            return events.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            events.offer(STOP_EVENT);
        }
    }

    public void stopLater() {
        if (running.get()) {
            events.offer(STOP_EVENT);
        }
    }

    public void clearEvents() {
        events.clear();
    }

    public void removeListeners() {
        listeners.clear();
    }

}
