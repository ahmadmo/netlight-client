package org.netlight.client;

import org.netlight.channel.ChannelContext;
import org.netlight.channel.RichChannelHandler;
import org.netlight.messaging.MessagePromise;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * @author ahmad
 */
public interface ClientHandler extends RichChannelHandler {

    ChannelContext getConnectionContext(SocketAddress remoteAddress);

    void sendMessage(SocketAddress remoteAddress, MessagePromise promise);

    void sendMessages(SocketAddress remoteAddress, Collection<MessagePromise> promises);

}
