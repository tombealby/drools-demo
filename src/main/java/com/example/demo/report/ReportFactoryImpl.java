package com.example.demo.report;

import java.util.Arrays;
import java.util.List;

import com.example.demo.report.Message.Type;

public class ReportFactoryImpl implements ReportFactory {

    @Override
    public ValidationReport createValidationReport() {
        return new ValidationReportImpl();
    }

    @Override
    public Message createMessage(Type type, String messageKey, Object... context) {

        final List<Object> contextList = Arrays.asList(context);
        return new MessageImpl(type, messageKey, contextList) ;
    }

}
