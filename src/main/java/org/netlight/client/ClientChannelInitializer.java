package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import org.netlight.encoding.EncodingProtocol;
import org.netlight.messaging.MessageQueueLoopGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author ahmad
 */
public final class ClientChannelInitializer extends ChannelInitializer<Channel> {

    private final SocketAddress remoteAddress;
    private final SslContext sslCtx;
    private final TcpChannelInitializer tcpChannelInitializer;

    public ClientChannelInitializer(SocketAddress remoteAddress, SslContext sslCtx,
                                    EncodingProtocol protocol, MessageQueueLoopGroup loopGroup) {
        Objects.requireNonNull(remoteAddress);
        this.remoteAddress = remoteAddress;
        this.sslCtx = sslCtx;
        this.tcpChannelInitializer = new TcpChannelInitializer(protocol, loopGroup);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            InetSocketAddress address = (InetSocketAddress) this.remoteAddress;
            p.addLast(sslCtx.newHandler(ch.alloc(), address.getAddress().getHostAddress(), address.getPort()));
        }
        p.addLast(tcpChannelInitializer);
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public SslContext getSslContext() {
        return sslCtx;
    }

    public TcpChannelInitializer getTcpChannelInitializer() {
        return tcpChannelInitializer;
    }

}
