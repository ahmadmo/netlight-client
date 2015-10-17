package org.netlight.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.netlight.client.messaging.MessagePromise;

import java.net.SocketAddress;

/**
 * @author ahmad
 */
public final class NettyConnectionContext implements ConnectionContext {

    private static final long serialVersionUID = 2083845290633467393L;

    private final String id;
    private final ChannelHandlerContext channelHandlerContext;
    private final ClientHandler clientHandler;

    public NettyConnectionContext(String id, ChannelHandlerContext channelHandlerContext, ClientHandler clientHandler) {
        this.id = id;
        this.channelHandlerContext = channelHandlerContext;
        this.clientHandler = clientHandler;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ChannelHandlerContext channelHandlerContext() {
        return channelHandlerContext;
    }

    @Override
    public Channel channel() {
        return channelHandlerContext.channel();
    }

    @Override
    public ClientHandler clientHandler() {
        return clientHandler;
    }

    @Override
    public SocketAddress remoteAddress() {
        return channelHandlerContext.channel().remoteAddress();
    }

    @Override
    public void sendMessage(MessagePromise promise) {
        clientHandler.sendMessage(channelHandlerContext, promise);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj instanceof NettyConnectionContext && id.equals(((NettyConnectionContext) obj).id);
    }

}
