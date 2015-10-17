package org.netlight;

import org.netlight.client.messaging.Message;
import org.netlight.util.serialization.JSONSerializer;
import org.netlight.util.serialization.JavaSerializer;
import org.netlight.util.serialization.KryoSerializer;
import org.netlight.util.serialization.ObjectSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ahmad
 */
public final class SerializerTest {

    static final List<ObjectSerializer<Message>> SERIALIZERS = Arrays.asList(
            new JavaSerializer<>(Message.class), new JSONSerializer<>(Message.class), new KryoSerializer<>(Message.class)
    );

    public static void main(String[] args) throws Exception {
        Message message = buildMessage(new Message());
        int iterationCount = 1000;

        //warp up
        System.out.println("warm up...");
        for (int i = 0; i < iterationCount; i++) {
            for (ObjectSerializer<Message> serializer : SERIALIZERS) {
                serializer.deserialize(serializer.serialize(message));
            }
        }

        System.out.println("test started...");
        for (ObjectSerializer<Message> serializer : SERIALIZERS) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < iterationCount; i++) {
                if (!serializer.deserialize(serializer.serialize(message)).equals(message)) {
                    System.out.printf("%s test FAILED!", serializer.getClass().getSimpleName());
                    break;
                }
            }
            long end = System.currentTimeMillis();
            System.out.printf("%s test took %d%n", serializer.getClass().getSimpleName(), end - start);
        }

    }

    static Message buildMessage(Message message) {
        for (int i = 0; i < 10; i++) {
            Message m = new Message();
            m.put("now", System.currentTimeMillis());
            for (int j = 0; j < 10; j++) {
                List<Double> list = new ArrayList<>();
                for (int k = 0; k < 100; k++) {
                    list.add(ThreadLocalRandom.current().nextDouble());
                }
                m.put(UUID.randomUUID().toString(), list);
            }
            message.put(String.valueOf(message.size()), m);
        }
        return message;
    }

}
