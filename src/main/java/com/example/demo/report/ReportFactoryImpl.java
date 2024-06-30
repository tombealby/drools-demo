package com.example.demo.report;

import java.util.Arrays;
import java.util.List;

import com.example.demo.report.Message.Type;

public class ReportFactoryImpl implements ReportFactory {
    
    private ValidationReport validationReport;

    @Override
    public ValidationReport createValidationReport() {
        if (validationReport == null) {
            validationReport = new ValidationReportImpl();
        }
        return validationReport;
    }

    @Override
    public Message createMessage(Type type, String messageKey, Object... context) {

        final List<Object> contextList = Arrays.asList(context);
        return new MessageImpl(type, messageKey, contextList) ;
    }
    
    public ValidationReport getValidationReport() {
        return validationReport;
    }

}
