package org.netlight.encoding;

import org.netlight.messaging.Message;
import org.netlight.util.serialization.BinaryObjectSerializer;
import org.netlight.util.serialization.JavaSerializer;
import org.netlight.util.serialization.KryoSerializer;

/**
 * @author ahmad
 */
public final class StandardBinarySerializers {

    private StandardBinarySerializers() {
    }

    public static final BinaryObjectSerializer<Message> JAVA = new JavaSerializer<>(Message.class);
    public static final BinaryObjectSerializer<Message> KRYO = new KryoSerializer<>(Message.class);

}
