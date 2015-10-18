package org.netlight.client;

import io.netty.channel.ChannelFuture;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.netlight.client.encoding.StandardSerializers;
import org.netlight.client.messaging.*;
import org.netlight.util.CommonUtils;
import org.netlight.util.EventNotifier;
import org.netlight.util.EventNotifierHandler;
import org.netlight.util.TimeProperty;
import org.netlight.util.concurrent.AtomicBooleanField;
import org.netlight.util.concurrent.AtomicLongField;
import org.netlight.util.serialization.ObjectSerializer;

import javax.net.ssl.SSLException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.netlight.client.ChannelState.*;

/**
 * @author ahmad
 */
public final class Connector implements AutoCloseable {

    private static final ObjectSerializer<Message> DEFAULT_OBJECT_SERIALIZER = StandardSerializers.JSON;

    private static final String MESSAGE_ID = "message_id";
    private static final String CORRELATION_ID = "correlation_id";
    private static final AtomicLongField ID = new AtomicLongField();

    private final SocketAddress remoteAddress;
    private final MessageQueueLoopGroup loopGroup;
    private final Client client;
    private final ClientHandler clientHandler;
    private final MessageHandler messageHandler = new MessageHandler();
    private final EventNotifier<Message, ServerSentMessageListener> serverSentMessageNotifier;
    private final AtomicBooleanField closed = new AtomicBooleanField();

    public Connector(SocketAddress remoteAddress) {
        this(remoteAddress, null, DEFAULT_OBJECT_SERIALIZER);
    }

    public Connector(SocketAddress remoteAddress, TimeProperty autoReconnectInterval) {
        this(remoteAddress, autoReconnectInterval, DEFAULT_OBJECT_SERIALIZER);
    }

    public Connector(SocketAddress remoteAddress, ObjectSerializer<Message> serializer) {
        this(remoteAddress, null, serializer);
    }

    public Connector(SocketAddress remoteAddress, TimeProperty autoReconnectInterval, ObjectSerializer<Message> serializer) {
        this.remoteAddress = remoteAddress;
        loopGroup = new MessageQueueLoopGroup(messageHandler, new SingleMessageQueueStrategy(), new LoopShiftingStrategy());
        client = new NettyClient(remoteAddress, getSslContext(), serializer, loopGroup);
        if (autoReconnectInterval != null) {
            client.addChannelStateListener(new AutoReconnector(autoReconnectInterval.to(TimeUnit.MILLISECONDS)));
        }
        clientHandler = client.getChannelInitializer().getTcpChannelInitializer().getHandler();
        serverSentMessageNotifier = new EventNotifier<>(new EventNotifierHandler<Message, ServerSentMessageListener>() {
            @Override
            public void handle(Message message, ServerSentMessageListener listener) {
                listener.onMessage(message);
            }

            @Override
            public void exceptionCaught(Throwable cause) {
                serverSentMessageNotifier.start();
            }
        }, Message.class);
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public boolean connect() {
        return client.connect();
    }

    public CompletableFuture<Boolean> connectAsync() {
        return CompletableFuture.supplyAsync(this::connect);
    }

    public ChannelFuture closeFuture() {
        return client.closeFuture();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public MessageFuture send(Message message) {
        return messageHandler.send(message);
    }

    public void addChannelStateListener(ChannelStateListener listener) {
        client.addChannelStateListener(listener);
    }

    public void removeChannelStateListener(ChannelStateListener listener) {
        client.removeChannelStateListener(listener);
    }

    public void addServerSentMessageListener(ServerSentMessageListener listener) {
        serverSentMessageNotifier.addListener(listener);
    }

    public void removeServerSentMessageListener(ServerSentMessageListener listener) {
        serverSentMessageNotifier.removeListener(listener);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            ((NettyClient) client).close();
            loopGroup.shutdownGracefully();
        }
    }

    public void diconnect() {
        close();
    }

    private final class MessageHandler implements MessageQueueLoopHandler {

        private final Map<Long, MessagePromise> messageTracking = new ConcurrentHashMap<>();

        private MessageFuture send(Message message) {
            Objects.requireNonNull(message);
            long id = ID.incrementAndGet();
            message.put(MESSAGE_ID, id);
            MessagePromise promise = new DefaultMessagePromise(message, remoteAddress);
            messageTracking.put(id, promise);
            clientHandler.sendMessage(remoteAddress, promise);
            return promise;
        }

        @Override
        public void onMessage(MessageQueueLoop loop, Message message) {
            Long id = message.getLong(CORRELATION_ID);
            MessagePromise promise;
            if (id == null || (promise = messageTracking.remove(id)) == null) {
                serverSentMessageNotifier.notify(message);
            } else {
                promise.setResponse(message);
            }
        }

        @Override
        public void exceptionCaught(MessageQueueLoop loop, Message message, Throwable cause) {
            cause.printStackTrace(); // TODO log
        }

    }

    private final class AutoReconnector implements ChannelStateListener {

        private final long delay;

        private AutoReconnector(long delay) {
            this.delay = delay;
        }

        @Override
        public void stateChanged(ChannelState state, Client client) {
            if (state == CONNECTED) {
                closed.set(false);
                serverSentMessageNotifier.start();
            } else if (state == DISCONNECTED || state == CONNECTION_FAILED && !closed.get()) {
                serverSentMessageNotifier.stopLater();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!closed.get()) {
                            client.connect();
                        }
                    }
                }, delay);
            }
        }

    }

    public static final class ConnectorBuilder {

        private SocketAddress remoteAddress;
        private TimeProperty autoReconnectInterval;
        private ObjectSerializer<Message> serializer;

        public ConnectorBuilder(SocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        public ConnectorBuilder remoteAddress(SocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public ConnectorBuilder autoReconnect(TimeProperty autoReconnectInterval) {
            this.autoReconnectInterval = autoReconnectInterval;
            return this;
        }

        public ConnectorBuilder serializer(ObjectSerializer<Message> serializer) {
            this.serializer = serializer;
            return this;
        }

        public Connector build() {
            return new Connector(remoteAddress, autoReconnectInterval, CommonUtils.getOrDefault(serializer, DEFAULT_OBJECT_SERIALIZER));
        }

    }

    public static ConnectorBuilder to(SocketAddress remoteAddress) {
        return new ConnectorBuilder(remoteAddress);
    }

    private static SslContext getSslContext() {
        try {
            return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            return null;
        }
    }

}