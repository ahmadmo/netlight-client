package org.netlight.util.serialization;

/**
 * @author ahmad
 */
public interface ObjectSerializer<T> {

    byte[] serialize(T t) throws Exception;

    T deserialize(byte[] bytes) throws Exception;

    Class<T> getType();

}
