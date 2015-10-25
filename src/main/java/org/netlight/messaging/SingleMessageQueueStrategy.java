package org.netlight.messaging;

/**
 * @author ahmad
 */
public final class SingleMessageQueueStrategy implements MessageQueueStrategy {

    private final MessageQueue queue;

    public SingleMessageQueueStrategy() {
        this(new ConcurrentMessageQueue());
    }

    public SingleMessageQueueStrategy(MessageQueue queue) {
        this.queue = queue;
    }

    @Override
    public MessageQueue next() {
        return queue;
    }

}
