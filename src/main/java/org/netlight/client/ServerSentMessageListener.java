package org.netlight.client;

import org.netlight.client.messaging.Message;

/**
 * @author ahmad
 */
public interface ServerSentMessageListener {

    void onMessage(Message message);

}
