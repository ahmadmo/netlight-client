package org.netlight.client;

import org.netlight.channel.ChannelContext;
import org.netlight.channel.RichChannelHandler;
import org.netlight.messaging.Message;
import org.netlight.messaging.MessagePromise;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * @author ahmad
 */
public interface ClientHandler extends RichChannelHandler {

    ChannelContext getConnectionContext(SocketAddress remoteAddress);

    MessagePromise sendMessage(SocketAddress remoteAddress, Message message);

    void sendMessage(MessagePromise promise);

    Collection<MessagePromise> sendMessages(SocketAddress remoteAddress, Collection<Message> messages);

    void sendMessages(Collection<MessagePromise> promises);

}
