package org.netlight.client.messaging;

/**
 * @author ahmad
 */
public interface MessageFutureListener {

    void onComplete(MessageFuture future);

    void onResponse(MessageFuture future, Message message);

}
