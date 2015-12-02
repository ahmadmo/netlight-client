package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import org.netlight.channel.AbstractRichChannelHandler;
import org.netlight.channel.ChannelContext;
import org.netlight.channel.NetLightChannelContext;
import org.netlight.messaging.DefaultMessagePromise;
import org.netlight.messaging.Message;
import org.netlight.messaging.MessagePromise;
import org.netlight.messaging.MessageQueueLoopGroup;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class TcpClientHandler extends AbstractRichChannelHandler<Message> implements ClientHandler {

    private static final int FLUSH_COUNT = 5;

    private final MessageQueueLoopGroup loopGroup;
    private final Map<SocketAddress, ChannelContext> connections = new ConcurrentHashMap<>();
    private final Map<SocketAddress, Queue<MessagePromise>> pendingMessages = new ConcurrentHashMap<>();

    public TcpClientHandler(MessageQueueLoopGroup loopGroup) {
        Objects.requireNonNull(loopGroup);
        this.loopGroup = loopGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final SocketAddress remoteAddress = ctx.channel().remoteAddress();
        connections.put(remoteAddress, getConnectionContext(ctx));
        Queue<MessagePromise> queue = pendingMessages.remove(remoteAddress);
        if (queue != null) {
            sendMessages(ctx, queue);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connections.remove(ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (!msg.isEmpty()) {
            loopGroup.queueMessage(getConnectionContext(ctx), msg);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        if (channel.isWritable()) {
            sendMessages(ctx, pendingMessages.remove(channel.remoteAddress()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(); // TODO log
        ctx.close();
    }

    @Override
    public ChannelContext getConnectionContext(SocketAddress remoteAddress) {
        return connections.get(remoteAddress);
    }

    @Override
    public MessagePromise sendMessage(SocketAddress remoteAddress, Message message) {
        MessagePromise promise = newPromise(remoteAddress, message);
        sendMessage(promise);
        return promise;
    }

    @Override
    public void sendMessage(MessagePromise promise) {
        final SocketAddress remoteAddress = promise.remoteAddress();
        final ChannelContext ctx = connections.get(remoteAddress);
        if (ctx != null) {
            sendMessage(ctx.channelHandlerContext(), promise);
        } else {
            promise.setCancellable(true);
            getQueue(remoteAddress).offer(promise);
        }
    }

    @Override
    public Collection<MessagePromise> sendMessages(SocketAddress remoteAddress, Collection<Message> messages) {
        Collection<MessagePromise> promises = newPromises(remoteAddress, messages);
        sendMessages(promises);
        return promises;
    }

    @Override
    public void sendMessages(Collection<MessagePromise> promises) {
        if (promises.isEmpty()) {
            return;
        }
        final SocketAddress remoteAddress = promises.iterator().next().remoteAddress();
        final ChannelContext ctx = connections.get(remoteAddress);
        if (ctx != null) {
            sendMessages(ctx.channelHandlerContext(), promises);
        } else {
            promises.forEach(p -> p.setCancellable(true));
            enqueueMessages(remoteAddress, promises);
        }
    }

    @Override
    public void sendMessage(ChannelHandlerContext ctx, MessagePromise promise) {
        if (ctx == null || promise == null || promise.message().isEmpty()) {
            return;
        }
        final Channel channel = ctx.channel();
        if (channel.isActive() && channel.isWritable()) {
            if (promise instanceof DefaultMessagePromise) {
                ctx.writeAndFlush(promise.message()).addListener(f -> completePromise(promise, f));
            } else {
                ctx.writeAndFlush(promise.message(), ctx.voidPromise());
            }
        } else {
            promise.setCancellable(true);
            getQueue(channel.remoteAddress()).offer(promise);
        }
    }

    @Override
    public void sendMessages(ChannelHandlerContext ctx, Collection<MessagePromise> promises) {
        if (ctx == null || promises == null || promises.isEmpty()) {
            return;
        }
        final Channel channel = ctx.channel();
        if (channel.isActive() && channel.isWritable()) {
            channel.eventLoop().execute(new BatchMessageSender(ctx, promises));
        } else {
            promises.forEach(p -> p.setCancellable(true));
            enqueueMessages(channel.remoteAddress(), promises);
        }
    }

    private ChannelContext getConnectionContext(ChannelHandlerContext ctx) {
        final String id = ctx.channel().toString();
        final SocketAddress remoteAddress = ctx.channel().remoteAddress();
        ChannelContext context = connections.get(remoteAddress);
        if (context == null) {
            final ChannelContext c = connections.putIfAbsent(remoteAddress, context = new NetLightChannelContext(id, ctx, this));
            if (c != null) {
                context = c;
            }
        }
        return context;
    }

    private Queue<MessagePromise> getQueue(SocketAddress key) {
        Queue<MessagePromise> queue = pendingMessages.get(key);
        if (queue == null) {
            final Queue<MessagePromise> q = pendingMessages.putIfAbsent(key, queue = new ConcurrentLinkedQueue<>());
            if (q != null) {
                queue = q;
            }
        }
        return queue;
    }

    private void enqueueMessages(SocketAddress key, Collection<MessagePromise> promises) {
        Queue<MessagePromise> queue = pendingMessages.get(key);
        if (queue == null) {
            queue = pendingMessages.putIfAbsent(key, promises instanceof ConcurrentLinkedQueue
                    ? (Queue<MessagePromise>) promises
                    : new ConcurrentLinkedQueue<>(promises));
        }
        if (queue != null) {
            queue.addAll(promises);
        }
    }

    private final class BatchMessageSender implements Runnable {

        private final ChannelHandlerContext ctx;
        private final Queue<MessagePromise> promises;

        private BatchMessageSender(ChannelHandlerContext ctx, Collection<MessagePromise> promises) {
            this.ctx = ctx;
            this.promises = promises instanceof ConcurrentLinkedQueue
                    ? (Queue<MessagePromise>) promises
                    : new ConcurrentLinkedQueue<>(promises);
        }

        @Override
        public void run() {
            final Channel channel = ctx.channel();
            MessagePromise promise;
            while (!promises.isEmpty() && channel.isActive() && channel.isWritable()) {
                for (int i = 0; i < FLUSH_COUNT && (promise = promises.poll()) != null; i++) {
                    if (!promise.isCancelled() && !promise.message().isEmpty()) {
                        if (promise instanceof DefaultMessagePromise) {
                            final MessagePromise p = promise;
                            ctx.write(p.message()).addListener(f -> completePromise(p, f));
                        } else {
                            ctx.write(promise.message(), ctx.voidPromise());
                        }
                    }
                }
                ctx.flush();
            }
            if (!promises.isEmpty()) {
                promises.forEach(p -> p.setCancellable(true));
                enqueueMessages(channel.remoteAddress(), promises);
            }
        }

    }

    private static void completePromise(MessagePromise p, Future<? super Void> f) {
        if (f.isSuccess()) {
            p.setSuccess();
        } else {
            p.setFailure(f.cause());
        }
    }

}