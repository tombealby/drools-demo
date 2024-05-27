package com.example.demo.report;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.report.Message.Type;

public class ReportFactoryImpl implements ReportFactory {

    @Override
    public ValidationReport createValidationReport() {
        return new ValidationReportImpl();
    }

    @Override
    public Message createMessage(Type type, String messageKey, Object... context) {

        return new Message() {

            @Override
            public Type getType() {
                return Message.Type.ERROR;
            }

            @Override
            public String getMessageKey() {
                return "my message key";
            }

            @Override
            public List<Object> getContextOrdered() {
                return new ArrayList<>();
            }

        };
    }

}
