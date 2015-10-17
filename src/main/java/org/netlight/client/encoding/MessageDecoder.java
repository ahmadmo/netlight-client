package org.netlight.client.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.netlight.client.messaging.Message;
import org.netlight.util.serialization.ObjectSerializer;

import java.util.List;
import java.util.Objects;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final ObjectSerializer<Message> serializer;

    public MessageDecoder(ObjectSerializer<Message> serializer) {
        Objects.requireNonNull(serializer);
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] bytes;
        if (msg.hasArray()) {
            bytes = msg.array();
        } else {
            bytes = new byte[msg.readableBytes()];
            msg.getBytes(msg.readerIndex(), bytes);
        }
        try {
            out.add(serializer.deserialize(bytes));
        } catch (Exception e) {
            e.printStackTrace(); // TODO log
        }
    }

}
