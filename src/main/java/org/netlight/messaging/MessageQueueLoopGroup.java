package org.netlight.messaging;

import org.netlight.client.ConnectionContext;
import org.netlight.util.TimeProperty;
import org.netlight.util.concurrent.AtomicBooleanField;
import org.netlight.util.concurrent.CacheManager;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ahmad
 */
public final class MessageQueueLoopGroup {

    private final ExecutorService executorService;
    private final MessageQueueLoopHandler handler;
    private final MessageQueueStrategy queueStrategy;
    private final MessageQueueLoopStrategy loopStrategy;
    private final CacheManager<ConnectionContext, MessageQueueLoop> loops;
    private final AtomicBooleanField looping = new AtomicBooleanField(true);

    public MessageQueueLoopGroup(ExecutorService executorService, MessageQueueLoopHandler handler,
                                 MessageQueueStrategy queueStrategy, MessageQueueLoopStrategy loopStrategy) {
        Objects.requireNonNull(executorService);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(queueStrategy);
        Objects.requireNonNull(loopStrategy);
        this.executorService = executorService;
        this.handler = handler;
        this.queueStrategy = queueStrategy;
        this.loopStrategy = loopStrategy;
        loops = CacheManager.<ConnectionContext, MessageQueueLoop>newBuilder()
                .expireAfterAccess(TimeProperty.minutes(5))
                .removalListener(notification -> {
                    MessageQueueLoop loop = notification.getValue();
                    if (loop != null) {
                        loop.stop();
                    }
                }).build();
    }

    public void queueMessage(ConnectionContext ctx, Message message) {
        final MessageQueueLoop loop = getLoop(ctx);
        loop.getMessageQueue().add(message);
        loop.getLoopStrategy().poke();
        if (!loop.isLooping()) {
            executorService.execute(loop);
        }
    }

    private MessageQueueLoop getLoop(ConnectionContext ctx) {
        MessageQueueLoop loop = loops.retrieve(ctx);
        if (loop == null) {
            final MessageQueueLoop l = loops.cacheIfAbsent(ctx, loop = new MessageQueueLoop(ctx, queueStrategy.next(), handler, loopStrategy));
            if (l != null) {
                loop = l;
            }
        }
        return loop;
    }

    public boolean isLooping() {
        return looping.get();
    }

    public boolean shutdownGracefully() {
        if (looping.compareAndSet(true, false)) {
            executorService.shutdown();
            loops.forEachValue(MessageQueueLoop::stop);
            executorService.shutdownNow();
            try {
                return executorService.awaitTermination(15L, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

}
