package org.netlight.client.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.netlight.client.messaging.Message;
import org.netlight.util.serialization.ObjectSerializer;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class MessageEncoder extends MessageToByteEncoder<Message> {

    private final ObjectSerializer<Message> serializer;

    public MessageEncoder() {
        this(StandardSerializers.KRYO);
    }

    public MessageEncoder(ObjectSerializer<Message> serializer) {
        Objects.requireNonNull(serializer);
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg != null) {
            try {
                wrapBuffer(out, serializer.serialize(msg));
            } catch (Exception e) {
                e.printStackTrace(); // TODO log
            }
        }
    }

    private static ByteBuf wrapBuffer(ByteBuf dst, byte[] bytes) {
        ByteBuffer dstBuffer = dst.internalNioBuffer(0, bytes.length);
        final int pos = dstBuffer.position();
        dstBuffer.put(bytes);
        dst.writerIndex(dst.writerIndex() + dstBuffer.position() - pos);
        return dst;
    }

}