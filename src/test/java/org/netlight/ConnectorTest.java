package org.netlight;

import org.netlight.client.Connector;
import org.netlight.encoding.MessageEncodingProtocol;
import org.netlight.messaging.Message;
import org.netlight.messaging.MessageFuture;
import org.netlight.messaging.MessageFutureListener;
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
                .messageEncodingProtocol(MessageEncodingProtocol.JSON)
                .build();

        connector.addChannelStateListener((state) -> {
            switch (state) {
                case OPENED:
                    System.out.println("SUCCESSFULLY CONNECTED TO : " + connector.getRemoteAddress());
                    break;
                case CLOSED:
                    System.out.println("DISCONNECTED FROM : " + connector.getRemoteAddress());
                    break;
                case OPEN_FAILURE:
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
                if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
                    break;
                }
                Message message = new Message();
                message.put("user_input", input);
                connector.send(message).addListener(listener);
            }
        } finally {
            connector.close();
        }
        System.out.println("END");
        System.exit(0);
    }

    static final class MessageFutureListenerImpl implements MessageFutureListener {

        @Override
        public void onComplete(MessageFuture future) {
            Number id = future.message().getNumber("message_id");
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
            System.out.println(future.remoteAddress() + " (reply to = " + message.getNumber("correlation_id") + ") : " + message.getString("reply"));
        }

    }

}
