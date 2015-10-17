package org.netlight.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.netlight.client.messaging.MessagePromise;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * @author ahmad
 */
public interface ClientHandler extends ChannelHandler {

    ConnectionContext getConnectionContext(SocketAddress remoteAddress);

    void sendMessage(SocketAddress remoteAddress, MessagePromise promise);

    void sendMessage(ChannelHandlerContext ctx, MessagePromise promise);

    void sendMessages(ChannelHandlerContext ctx, Collection<MessagePromise> promises);

}
