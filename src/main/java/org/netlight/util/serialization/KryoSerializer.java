package org.netlight.util.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;

/**
 * @author ahmad
 */
public final class KryoSerializer<T> implements ObjectSerializer<T> {

    private final Class<T> type;
    private final Kryo kryo = new Kryo();

    public KryoSerializer(Class<T> type) {
        this.type = type;
        kryo.register(type);
    }

    public Class<T> getType() {
        return type;
    }

    public <S> void register(Class<S> type) {
        kryo.register(type);
    }

    public <S> void register(Class<S> type, int id) {
        kryo.register(type, id);
    }

    public <S> void register(Class<S> type, Serializer<S> serializer) {
        kryo.register(type, serializer);
    }

    public <S> void register(Class<S> type, Serializer<S> serializer, int id) {
        kryo.register(type, serializer, id);
    }

    @Override
    public byte[] serialize(T t) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            kryo.writeObject(output, t);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        try (Input input = new Input(bytes)) {
            return kryo.readObject(input, type);
        }
    }

}