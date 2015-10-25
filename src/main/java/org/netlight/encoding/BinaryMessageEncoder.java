package org.netlight.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.netlight.messaging.Message;
import org.netlight.util.serialization.BinaryObjectSerializer;

import java.util.Objects;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class BinaryMessageEncoder extends MessageToByteEncoder<Message> {

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    private final BinaryObjectSerializer<Message> serializer;

    public BinaryMessageEncoder(BinaryObjectSerializer<Message> serializer) {
        Objects.requireNonNull(serializer);
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        out.writeBytes(LENGTH_PLACEHOLDER);
        out.writeBytes(serializer.serialize(msg));
        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }

}