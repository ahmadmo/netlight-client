package org.netlight.client.encoding;

import org.netlight.client.messaging.Message;
import org.netlight.util.serialization.JSONSerializer;
import org.netlight.util.serialization.JavaSerializer;
import org.netlight.util.serialization.KryoSerializer;
import org.netlight.util.serialization.ObjectSerializer;

/**
 * @author ahmad
 */
public final class StandardSerializers {

    private StandardSerializers() {
    }

    public static final ObjectSerializer<Message> JAVA = new JavaSerializer<>(Message.class);
    public static final ObjectSerializer<Message> JSON = new JSONSerializer<>(Message.class);
    public static final ObjectSerializer<Message> KRYO = new KryoSerializer<>(Message.class);

}
