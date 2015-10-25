package org.netlight.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.netlight.util.CommonUtils.notNull;

/**
 * @author ahmad
 */
public final class MessageBuilder {

    private final Message message;

    private MessageBuilder(Message message) {
        this.message = message;
    }

    public static MessageBuilder builder() {
        return builder(new Message());
    }

    public static MessageBuilder builder(Message message) {
        return new MessageBuilder(message);
    }

    public MessageBuilder put(String name, Object value) {
        if (notNull(value)) {
            Objects.requireNonNull(name);
            message.put(name, value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MessageBuilder putToList(String name, Object value) {
        if (notNull(value)) {
            Objects.requireNonNull(name);
            List<Object> list = message.getList(name);
            if (list == null) {
                message.put(name, list = new ArrayList<>());
            }
            list.add(value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MessageBuilder putToList(String name, Object... values) {
        if (values.length > 0) {
            Objects.requireNonNull(name);
            List<Object> list = message.getList(name);
            if (list == null) {
                message.put(name, list = new ArrayList<>());
            }
            Collections.addAll(list, values);
        }
        return this;
    }

    public boolean isEmpty() {
        return message.isEmpty();
    }

    public Message build() {
        return message;
    }

}
