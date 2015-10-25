package org.netlight.encoding;

import org.netlight.messaging.Message;
import org.netlight.util.concurrent.AtomicReferenceField;
import org.netlight.util.serialization.BinaryObjectSerializer;

/**
 * @author ahmad
 */
public enum BinaryEncodingProtocol implements EncodingProtocol {

    JAVA(StandardBinarySerializers.JAVA),
    KRYO(StandardBinarySerializers.KRYO);

    private final BinaryObjectSerializer<Message> serializer;
    private final AtomicReferenceField<BinaryMessageEncoder> encoder = new AtomicReferenceField<>();

    BinaryEncodingProtocol(BinaryObjectSerializer<Message> serializer) {
        this.serializer = serializer;
    }

    @Override
    public BinaryMessageDecoder decoder() {
        return new BinaryMessageDecoder(serializer);
    }

    @Override
    public BinaryMessageEncoder encoder() {
        encoder.compareAndSet(null, new BinaryMessageEncoder(serializer));
        return encoder.get();
    }

}
