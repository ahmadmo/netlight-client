package org.netlight.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.netlight.client.messaging.Message;
import org.netlight.client.messaging.MessageQueueLoopGroup;
import org.netlight.util.EventNotifier;
import org.netlight.util.EventNotifierHandler;
import org.netlight.util.OSValidator;
import org.netlight.util.concurrent.AtomicBooleanField;
import org.netlight.util.concurrent.AtomicReferenceField;
import org.netlight.util.serialization.ObjectSerializer;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author ahmad
 */
public final class NettyClient implements Client {

    private final SocketAddress remoteAddress;
    private final SslContext sslCtx;
    private final ClientChannelInitializer channelInitializer;
    private final AtomicReferenceField<Channel> channel = new AtomicReferenceField<>();
    private final AtomicReferenceField<EventLoopGroup> group = new AtomicReferenceField<>();
    private final AtomicBooleanField connected = new AtomicBooleanField(false);
    private final EventNotifier<ChannelState, ChannelStateListener> channelStateNotifier;

    public NettyClient(SocketAddress remoteAddress, SslContext sslCtx, ObjectSerializer<Message> serializer, MessageQueueLoopGroup loopGroup) {
        Objects.requireNonNull(remoteAddress);
        this.remoteAddress = remoteAddress;
        this.sslCtx = sslCtx;
        this.channelInitializer = new ClientChannelInitializer(remoteAddress, sslCtx, serializer, loopGroup);
        channelStateNotifier = new EventNotifier<>(new EventNotifierHandler<ChannelState, ChannelStateListener>() {
            @Override
            public void handle(ChannelState event, ChannelStateListener listener) {
                listener.stateChanged(event, NettyClient.this);
            }

            @Override
            public void exceptionCaught(Throwable cause) {
                channelStateNotifier.start();
            }
        }, ChannelState.class);
    }

    @Override
    public boolean connect() {
        if (connected.get()) {
            return true;
        }
        channelStateNotifier.start();
        final Bootstrap b = configureBootstrap(new Bootstrap());
        try {
            final Channel ch = b.connect().sync().channel();
            connected.set(true);
            channel.set(ch);
            group.set(b.group());
            ch.closeFuture().addListener(f -> closed());
            fireChannelStateChanged(ChannelState.CONNECTED);
            return true;
        } catch (Exception e) {
            connected.set(false);
            fireChannelStateChanged(ChannelState.CONNECTION_FAILED);
        }
        return false;
    }

    private void closed() {
        connected.set(false);
        channel.set(null);
        EventLoopGroup g = group.getAndSet(null);
        if (g != null) {
            g.shutdownGracefully();
        }
        fireChannelStateChanged(ChannelState.DISCONNECTED);
        channelStateNotifier.stopLater();
    }

    private Bootstrap configureBootstrap(Bootstrap b) {
        return configureBootstrap(b, OSValidator.isUnix() ? new EpollEventLoopGroup() : new NioEventLoopGroup());
    }

    private Bootstrap configureBootstrap(Bootstrap b, EventLoopGroup g) {
        b.group(g)
                .channel(OSValidator.isUnix() ? EpollSocketChannel.class : NioSocketChannel.class)
                .remoteAddress(remoteAddress)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(channelInitializer);
        return b;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public ChannelFuture closeFuture() {
        Channel ch = channel.get();
        return ch == null ? null : ch.closeFuture();
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public SslContext getSslContext() {
        return sslCtx;
    }

    @Override
    public ClientChannelInitializer getChannelInitializer() {
        return channelInitializer;
    }

    @Override
    public void addChannelStateListener(ChannelStateListener channelStateListener) {
        channelStateNotifier.addListener(channelStateListener);
    }

    @Override
    public void removeChannelStateListener(ChannelStateListener channelStateListener) {
        channelStateNotifier.removeListener(channelStateListener);
    }

    @Override
    public void fireChannelStateChanged(ChannelState state) {
        channelStateNotifier.notify(state);
    }

    @Override
    public void close() {
        final Channel ch = channel.getAndSet(null);
        if (ch != null) {
            ch.close().awaitUninterruptibly();
        }
    }

}
