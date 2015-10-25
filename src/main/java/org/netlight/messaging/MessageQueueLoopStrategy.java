package org.netlight.messaging;

/**
 * @author ahmad
 */
public interface MessageQueueLoopStrategy {

    Message next(MessageQueue queue);

    void poke();

    boolean stopIfEmpty();

}
