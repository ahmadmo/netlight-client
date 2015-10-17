package org.netlight.client;

/**
 * @author ahmad
 */
public interface ChannelStateListener {

    void stateChanged(ChannelState state, Client client);

}
