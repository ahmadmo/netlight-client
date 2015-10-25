package org.netlight.messaging;

/**
 * @author ahmad
 */
public interface MessageFutureListener {

    void onComplete(MessageFuture future);

    void onResponse(MessageFuture future, Message message);

}
