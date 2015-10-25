package org.netlight.util.serialization;

import java.nio.charset.Charset;

/**
 * @author ahmad
 */
public interface TextObjectSerializer<I> extends ObjectSerializer<I, String> {

    Charset charset();

    @Override
    default Class<String> getOutputType() {
        return String.class;
    }

}
