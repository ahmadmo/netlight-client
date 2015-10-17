package org.netlight.client.messaging;

import org.netlight.util.TimeProperty;
import org.netlight.util.concurrent.FieldUpdater;

/**
 * @author ahmad
 */
public final class LoopShiftingStrategy implements MessageQueueLoopStrategy {

    public static final TimeProperty DEFAULT_SHIFTING_TIMEOUT = TimeProperty.millis(500L);

    private final TimeProperty timeout;
    private final FieldUpdater<Boolean> poke;

    public LoopShiftingStrategy() {
        this(DEFAULT_SHIFTING_TIMEOUT);
    }

    public LoopShiftingStrategy(TimeProperty timeout) {
        this.timeout = timeout;
        poke = new FieldUpdater<>(false, b -> false, timeout);
    }

    @Override
    public Message next(MessageQueue queue) {
        Message message = queue.poll(timeout);
        if (message == null && poke.compareAndSet(true, false)) {
            message = queue.poll(timeout);
        }
        return message;
    }

    @Override
    public void poke() {
        poke.set(true);
    }

    @Override
    public boolean stopIfEmpty() {
        return !poke.getAndSet(false);
    }

}
