package org.netlight.messaging;

import org.netlight.util.TimeProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author ahmad
 */
public final class ConcurrentMessageQueue implements MessageQueue {

    private final LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    @Override
    public boolean add(Message message) {
        return message != null && !message.isEmpty() && queue.offer(message);
    }

    @Override
    public boolean addAll(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (Message message : messages) {
            modified |= add(message);
        }
        return modified;
    }

    @Override
    public Message poll() {
        return queue.poll();
    }

    @Override
    public Message poll(TimeProperty timeout) {
        try {
            return queue.poll(timeout.getTime(), timeout.getUnit());
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public Message take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public Iterator<Message> iterator() {
        return queue.iterator();
    }

    @Override
    public List<Message> removeAll() {
        List<Message> messages = new ArrayList<>();
        Message m;
        while ((m = queue.poll()) != null) {
            messages.add(m);
        }
        return messages;
    }

    @Override
    public void tryStop() {
        queue.offer(STOP_MESSAGE);
    }

}
