package org.netlight.messaging;

import org.netlight.util.TimeProperty;

import java.util.Iterator;
import java.util.List;

/**
 * @author ahmad
 */
public interface MessageQueue {

    Message STOP_MESSAGE = new Message();

    boolean add(Message message);

    boolean addAll(List<Message> messages);

    Message poll();

    Message poll(TimeProperty timeout);

    Message take();

    Iterator<Message> iterator();

    List<Message> removeAll();

    void tryStop();

}
