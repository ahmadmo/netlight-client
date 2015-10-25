package org.netlight.messaging;

/**
 * @author ahmad
 */
public final class MessageQueuePerConnectionStrategy implements MessageQueueStrategy {

    @Override
    public MessageQueue next() {
        return new ConcurrentMessageQueue();
    }

}
