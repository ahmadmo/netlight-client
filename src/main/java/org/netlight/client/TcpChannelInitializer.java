package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.netlight.client.encoding.MessageDecoder;
import org.netlight.client.encoding.MessageEncoder;
import org.netlight.client.messaging.Message;
import org.netlight.client.messaging.MessageQueueLoopGroup;
import org.netlight.util.serialization.ObjectSerializer;

/**
 * @author ahmad
 */
public final class TcpChannelInitializer extends ChannelInitializer<Channel> {

    private final MessageDecoder decoder;
    private final MessageEncoder encoder;
    private final TcpClientHandler handler;

    public TcpChannelInitializer(ObjectSerializer<Message> serializer, MessageQueueLoopGroup loopGroup) {
        decoder = new MessageDecoder(serializer);
        encoder = new MessageEncoder(serializer);
        handler = new TcpClientHandler(loopGroup);
    }

    @Override
    public void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", decoder);
        p.addLast("encoder", encoder);
        p.addLast("handler", handler);
    }

    public MessageDecoder getDecoder() {
        return decoder;
    }

    public MessageEncoder getEncoder() {
        return encoder;
    }

    public TcpClientHandler getHandler() {
        return handler;
    }

}
