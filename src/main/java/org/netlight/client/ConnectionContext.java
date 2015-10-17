package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.netlight.client.messaging.MessagePromise;

import java.io.Serializable;
import java.net.SocketAddress;

/**
 * @author ahmad
 */
public interface ConnectionContext extends Serializable {

    String id();

    ChannelHandlerContext channelHandlerContext();

    Channel channel();

    ClientHandler clientHandler();

    SocketAddress remoteAddress();

    void sendMessage(MessagePromise promise);

}
