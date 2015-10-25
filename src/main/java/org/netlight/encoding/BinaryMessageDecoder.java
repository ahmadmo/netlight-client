package org.netlight.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.netlight.messaging.Message;
import org.netlight.util.serialization.BinaryObjectSerializer;

import java.util.Objects;

/**
 * @author ahmad
 */
public final class BinaryMessageDecoder extends LengthFieldBasedFrameDecoder {

    private final BinaryObjectSerializer<Message> serializer;

    public BinaryMessageDecoder(BinaryObjectSerializer<Message> serializer) {
        this(1024 * 1024, serializer);
    }

    public BinaryMessageDecoder(int maxObjectSize, BinaryObjectSerializer<Message> serializer) {
        super(maxObjectSize, 0, 4, 0, 4);
        Objects.requireNonNull(serializer);
        this.serializer = serializer;
    }

    @Override
    protected Message decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        byte[] bytes = new byte[frame.readableBytes()];
        frame.readBytes(bytes);
        return serializer.deserialize(bytes);
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }

}
