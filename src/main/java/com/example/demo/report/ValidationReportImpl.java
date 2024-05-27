package com.example.demo.report;

import java.util.HashSet;
import java.util.Set;

import com.example.demo.report.Message.Type;

public class ValidationReportImpl implements ValidationReport {

    private Set<Message> messages = new HashSet<Message>();

    @Override
    public Set<Message> getMessages() {
        return messages;
    }

    @Override
    public Set<Message> getMessagesByType(Type type) {
        return messages;
    }

    @Override
    public boolean contains(String messageKey) {
        return true;
    }

    @Override
    public boolean addMessage(Message message) {
        return messages.add(message);
    }

}
