package org.netlight.encoding;

import org.netlight.util.concurrent.AtomicReferenceField;

/**
 * @author ahmad
 */
public final class JsonEncodingProtocol implements EncodingProtocol {

    public static final JsonEncodingProtocol INSTANCE = new JsonEncodingProtocol();

    private final AtomicReferenceField<JsonMessageEncoder> encoder = new AtomicReferenceField<>();

    private JsonEncodingProtocol() {
    }

    @Override
    public JsonMessageDecoder decoder() {
        return new JsonMessageDecoder();
    }

    @Override
    public JsonMessageEncoder encoder() {
        encoder.compareAndSet(null, new JsonMessageEncoder());
        return encoder.get();
    }

}
