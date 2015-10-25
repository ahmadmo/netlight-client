package org.netlight.util.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ahmad
 */
public final class JSONSerializer<I> implements TextObjectSerializer<I> {

    private final ObjectWriter writer;
    private final ObjectReader reader;
    private final Class<I> inputType;
    private final Charset charset;

    public JSONSerializer(Class<I> inputType) {
        this(inputType, StandardCharsets.UTF_8);
    }

    public JSONSerializer(Class<I> inputType, Charset charset) {
        this.charset = charset;
        final ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writerFor(inputType);
        reader = mapper.readerFor(inputType);
        this.inputType = inputType;
    }

    @Override
    public String serialize(I obj) throws Exception {
        return writer.writeValueAsString(obj);
    }

    @Override
    public I deserialize(String json) throws Exception {
        return reader.readValue(json);
    }

    @Override
    public Class<I> getInputType() {
        return inputType;
    }

    @Override
    public Charset charset() {
        return charset;
    }

}
