package org.netlight.messaging;

import org.netlight.client.ConnectionContext;
import org.netlight.util.concurrent.AtomicBooleanField;

import java.util.Objects;

/**
 * @author ahmad
 */
public final class MessageQueueLoop implements Runnable {

    private final ConnectionContext ctx;
    private final MessageQueue messageQueue;
    private final MessageQueueLoopHandler handler;
    private final MessageQueueLoopStrategy loopStrategy;
    private final AtomicBooleanField looping = new AtomicBooleanField(false);

    public MessageQueueLoop(ConnectionContext ctx, MessageQueue messageQueue,
                            MessageQueueLoopHandler handler, MessageQueueLoopStrategy loopStrategy) {
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(messageQueue);
        Objects.requireNonNull(loopStrategy);
        Objects.requireNonNull(handler);
        this.ctx = ctx;
        this.messageQueue = messageQueue;
        this.handler = handler;
        this.loopStrategy = loopStrategy;
    }

    @Override
    public void run() {
        if (looping.compareAndSet(false, true)) {
            Message message = null;
            boolean exceptional = false;
            try {
                while (!Thread.currentThread().isInterrupted() && looping.get()) {
                    message = loopStrategy.next(messageQueue);
                    if (message != null) {
                        if (message == MessageQueue.STOP_MESSAGE) {
                            if (looping.get()) {
                                messageQueue.tryStop();
                            } else {
                                break;
                            }
                        } else {
                            handler.onMessage(this, message);
                        }
                    } else if (loopStrategy.stopIfEmpty()) {
                        break;
                    }
                }
            } catch (Throwable cause) {
                exceptional = true;
                looping.set(false);
                handler.exceptionCaught(this, message, cause);
            } finally {
                if (!exceptional) {
                    looping.set(false);
                }
            }
        }
    }

    public ConnectionContext getConnectionContext() {
        return ctx;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public MessageQueueLoopHandler getHandler() {
        return handler;
    }

    public MessageQueueLoopStrategy getLoopStrategy() {
        return loopStrategy;
    }

    public boolean isLooping() {
        return looping.get();
    }

    public void stop() {
        if (looping.compareAndSet(true, false)) {
            messageQueue.tryStop();
        }
    }

    @Override
    public int hashCode() {
        return ctx.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj instanceof MessageQueueLoop && ctx.equals(((MessageQueueLoop) obj).ctx);
    }

}
