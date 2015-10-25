package org.netlight.util.serialization;

/**
 * @author ahmad
 */
public interface BinaryObjectSerializer<I> extends ObjectSerializer<I, byte[]> {

    @Override
    default Class<byte[]> getOutputType() {
        return byte[].class;
    }

}
