package org.netlight.util.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author ahmad
 */
public final class KryoSerializer<I> implements BinaryObjectSerializer<I> {

    private final Class<I> inputType;
    private final Kryo kryo = new Kryo();

    public KryoSerializer(Class<I> inputType) {
        this.inputType = inputType;
        kryo.register(inputType);
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
    public byte[] serialize(I obj) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            kryo.writeObject(output, obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public I deserialize(byte[] bytes) throws IOException {
        try (Input input = new Input(bytes)) {
            return kryo.readObject(input, inputType);
        }
    }

    public Class<I> getInputType() {
        return inputType;
    }

}