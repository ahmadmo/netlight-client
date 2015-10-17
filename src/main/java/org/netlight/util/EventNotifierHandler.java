package org.netlight.util;

/**
 * @author ahmad
 */
public interface EventNotifierHandler<E, L> {

    void handle(E event, L listener);

    void exceptionCaught(Throwable cause);

}
