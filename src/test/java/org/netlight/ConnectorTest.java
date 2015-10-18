package org.netlight;

import org.netlight.client.Connector;
import org.netlight.client.encoding.StandardSerializers;
import org.netlight.client.messaging.Message;
import org.netlight.client.messaging.MessageFuture;
import org.netlight.client.messaging.MessageFutureListener;
import org.netlight.util.TimeProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @author ahmad
 */
public final class ConnectorTest {

    public static void main(String[] args) throws IOException {
        Connector connector = Connector.to(new InetSocketAddress("localhost", 18874))
                .autoReconnect(TimeProperty.seconds(5))
                .serializer(StandardSerializers.KRYO)
                .build();

        connector.addChannelStateListener((state, client) -> {
            switch (state) {
                case CONNECTED:
                    System.out.println("SUCCESSFULLY CONNECTED TO : " + connector.getRemoteAddress());
                    break;
                case DISCONNECTED:
                    System.out.println("DISCONNECTED FROM : " + connector.getRemoteAddress());
                    break;
                case CONNECTION_FAILED:
                    System.out.println("UNABLE TO CONNECT TO : " + connector.getRemoteAddress());
                    break;
            }
        });
        connector.addServerSentMessageListener(message -> System.out.println("SSE : " + message));
        connector.connectAsync();

        MessageFutureListener listener = new MessageFutureListenerImpl();
        String input;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while ((input = reader.readLine()) != null) {
                Message message = new Message();
                message.put("user_input", input);
                connector.send(message).addListener(listener);
            }
        } finally {
            connector.close();
        }
    }

    static final class MessageFutureListenerImpl implements MessageFutureListener {

        @Override
        public void onComplete(MessageFuture future) {
            Long id = future.message().getLong("message_id");
            if (!future.isSuccess()) {
                System.out.println("FAILED TO SEND MESSAGE! (id = " + id + ")");
                Throwable cause = future.cause();
                if (cause != null) {
                    System.err.println("DUE TO : " + cause.getMessage());
                }
            } else {
                System.out.println("MESSAGE SENT! (id = " + id + ")");
            }
        }

        @Override
        public void onResponse(MessageFuture future, Message message) {
            System.out.println(future.remoteAddress() + " (reply to = " + message.getLong("correlation_id") + ") : " + message.getString("reply"));
        }

    }

}
