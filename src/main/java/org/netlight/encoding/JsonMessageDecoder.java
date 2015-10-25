package org.netlight.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.RecyclableArrayList;

import java.util.List;

import static org.netlight.encoding.StandardTextSerializers.JSON;

/**
 * @author ahmad
 */
public final class JsonMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final JsonObjectDecoder decoder = new JsonObjectDecoder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        RecyclableArrayList buffers = RecyclableArrayList.newInstance();
        decoder.decode(ctx, msg, buffers);
        for (Object buf : buffers) {
            out.add(JSON.deserialize(((ByteBuf) buf).toString(JSON.charset())));
        }
        buffers.recycle();
    }

}
