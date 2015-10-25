package org.netlight.encoding;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

/**
 * @author ahmad
 */
public interface EncodingProtocol {

    ChannelInboundHandler decoder();

    ChannelOutboundHandler encoder();

}
