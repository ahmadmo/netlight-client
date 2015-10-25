package org.netlight.encoding;

import org.netlight.messaging.Message;
import org.netlight.util.serialization.JSONSerializer;
import org.netlight.util.serialization.TextObjectSerializer;

/**
 * @author ahmad
 */
public final class StandardTextSerializers {

    private StandardTextSerializers() {
    }

    public static final TextObjectSerializer<Message> JSON = new JSONSerializer<>(Message.class);

}
