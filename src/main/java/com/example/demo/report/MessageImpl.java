package com.example.demo.report;

import java.util.List;

public class MessageImpl implements Message {

    private Message.Type type;
    private String messageKey;
    private List<Object> context;

    public MessageImpl(Message.Type type, String messageKey, List<Object> context) {
        this.type = type;
        this.messageKey = messageKey;
        this.context = context;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public List<Object> getContextOrdered() {
        return context;
    }

}
