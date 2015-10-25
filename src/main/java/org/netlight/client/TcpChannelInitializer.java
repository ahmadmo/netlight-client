package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.netlight.encoding.EncodingProtocol;
import org.netlight.messaging.MessageQueueLoopGroup;

import java.util.Objects;

/**
 * @author ahmad
 */
public final class TcpChannelInitializer extends ChannelInitializer<Channel> {

    private final EncodingProtocol protocol;
    private final TcpClientHandler handler;

    public TcpChannelInitializer(EncodingProtocol protocol, MessageQueueLoopGroup loopGroup) {
        Objects.requireNonNull(protocol);
        this.protocol = protocol;
        handler = new TcpClientHandler(loopGroup);
    }

    @Override
    public void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", protocol.decoder());
        p.addLast("encoder", protocol.encoder());
        p.addLast("handler", handler);
    }

    public EncodingProtocol getProtocol() {
        return protocol;
    }

    public TcpClientHandler getHandler() {
        return handler;
    }

}
