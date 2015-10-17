package org.netlight.client;

import io.netty.channel.ChannelFuture;
import io.netty.handler.ssl.SslContext;

import java.net.SocketAddress;

/**
 * @author ahmad
 */
public interface Client extends AutoCloseable {

    boolean connect();

    boolean isConnected();

    ChannelFuture closeFuture();

    SocketAddress remoteAddress();

    SslContext getSslContext();

    ClientChannelInitializer getChannelInitializer();

    void addChannelStateListener(ChannelStateListener channelStateListener);

    void removeChannelStateListener(ChannelStateListener channelStateListener);

    void fireChannelStateChanged(ChannelState state);

}
