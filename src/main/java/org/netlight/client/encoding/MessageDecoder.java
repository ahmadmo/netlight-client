package org.netlight.client.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.netlight.client.messaging.Message;
import org.netlight.util.serialization.ObjectSerializer;

import java.util.List;
import java.util.Objects;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class MessageDecoder extends ByteToMessageDecoder {

    private final ObjectSerializer<Message> serializer;

    public MessageDecoder() {
        this(StandardSerializers.JSON);
    }

    public MessageDecoder(ObjectSerializer<Message> serializer) {
        Objects.requireNonNull(serializer);
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes;
        if (in.hasArray()) {
            bytes = in.array();
        } else {
            bytes = new byte[in.readableBytes()];
            in.getBytes(in.readerIndex(), bytes);
        }
        try {
            out.add(serializer.deserialize(bytes));
        } catch (Exception e) {
            e.printStackTrace(); // TODO log
        }
    }

}
