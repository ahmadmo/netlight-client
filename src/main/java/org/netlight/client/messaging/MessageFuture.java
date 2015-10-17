package org.netlight.client.messaging;

/**
 * @author ahmad
 */
public interface MessageFuture {

    Message getMessage();

    boolean isDone();

    boolean isSuccess();

    boolean isCancellable();

    void cancel();

    boolean isCancelled();

    Throwable cause();

    boolean hasResponse();

    Message getResponse();

    MessageFuture addListener(MessageFutureListener listener);

    MessageFuture removeListener(MessageFutureListener listener);

}
