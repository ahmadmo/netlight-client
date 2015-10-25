package org.netlight.messaging;

import java.net.SocketAddress;

/**
 * @author ahmad
 */
public interface MessageFuture {

    Message message();

    SocketAddress remoteAddress();

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
