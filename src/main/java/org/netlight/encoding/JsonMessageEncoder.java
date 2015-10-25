package org.netlight.encoding;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.netlight.messaging.Message;

import java.nio.CharBuffer;
import java.util.List;

import static org.netlight.encoding.StandardTextSerializers.JSON;

/**
 * @author ahmad
 */
@ChannelHandler.Sharable
public final class JsonMessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        if (msg != null) {
            out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(JSON.serialize(msg)), JSON.charset()));
        }
    }

}
