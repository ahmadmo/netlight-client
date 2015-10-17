package org.netlight.util.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author ahmad
 */
public final class JSONSerializer<T> implements ObjectSerializer<T> {

    private final Class<T> type;
    private final ObjectWriter writer;
    private final ObjectReader reader;

    public JSONSerializer(Class<T> type) {
        this.type = type;
        writer = new ObjectMapper().writerFor(type);
        reader = new ObjectMapper().reader(type);
    }

    @Override
    public byte[] serialize(T t) throws Exception {
        return writer.writeValueAsBytes(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws Exception {
        return reader.readValue(bytes);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
