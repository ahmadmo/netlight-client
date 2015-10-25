package org.netlight.messaging;

/**
 * @author ahmad
 */
public interface MessageQueueLoopHandler {

    void onMessage(MessageQueueLoop loop, Message message);

    void exceptionCaught(MessageQueueLoop loop, Message message, Throwable cause);

}
