package org.netlight.messaging;

/**
 * @author ahmad
 */
public interface MessagePromise extends MessageFuture {

    void setSuccess();

    void setSuccess(boolean success);

    void setCancellable(boolean cancellable);

    void setFailure(Throwable cause);

    void setResponse(Message response);

    @Override
    MessagePromise addListener(MessageFutureListener listener);

    @Override
    MessagePromise removeListener(MessageFutureListener listener);

}
