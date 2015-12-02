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
import org.netlight.channel.ChannelState;
import org.netlight.channel.ChannelStateListener;
import org.netlight.encoding.MessageEncodingProtocol;
import org.netlight.messaging.MessageQueueLoopGroup;
import org.netlight.util.EventNotifier;
import org.netlight.util.EventNotifierHandler;
import org.netlight.util.OSValidator;
import org.netlight.util.concurrent.AtomicBooleanField;
import org.netlight.util.concurrent.AtomicReferenceField;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author ahmad
 */
public final class NetLightClient implements Client {

    private final SocketAddress remoteAddress;
    private final SslContext sslCtx;
    private final ClientChannelInitializer channelInitializer;
    private final AtomicReferenceField<Channel> channel = new AtomicReferenceField<>();
    private final AtomicBooleanField connected = new AtomicBooleanField(false);
    private final AtomicReferenceField<ChannelState> state = new AtomicReferenceField<>(ChannelState.CLOSED);
    private final EventNotifier<ChannelState, ChannelStateListener> channelStateNotifier;

    public NetLightClient(SocketAddress remoteAddress, SslContext sslCtx, MessageEncodingProtocol messageEncodingProtocol, MessageQueueLoopGroup loopGroup) {
        Objects.requireNonNull(remoteAddress);
        this.remoteAddress = remoteAddress;
        this.sslCtx = sslCtx;
        this.channelInitializer = new ClientChannelInitializer(remoteAddress, sslCtx, messageEncodingProtocol, loopGroup);
        channelStateNotifier = new EventNotifier<>(new EventNotifierHandler<ChannelState, ChannelStateListener>() {
            @Override
            public void handle(ChannelState event, ChannelStateListener listener) {
                listener.stateChanged(event);
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
            ch.closeFuture().addListener(f -> closed(b.group()));
            fireChannelStateChanged(ChannelState.OPENED);
            return true;
        } catch (Exception e) {
            connected.set(false);
            fireChannelStateChanged(ChannelState.OPEN_FAILURE);
            channelStateNotifier.stopLater();
        }
        return false;
    }

    private void closed(EventLoopGroup g) {
        connected.set(false);
        channel.set(null);
        if (g != null) {
            g.shutdownGracefully();
        }
        fireChannelStateChanged(ChannelState.CLOSED);
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
    public ChannelState getChannelState() {
        return state.get();
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
        this.state.set(state);
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