package org.netlight.util.serialization;

/**
 * @author ahmad
 */
public interface ObjectSerializer<I, O> {

    O serialize(I in) throws Exception;

    I deserialize(O out) throws Exception;

    Class<I> getInputType();

    Class<O> getOutputType();

}
