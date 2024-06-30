package com.example.demo.report;

public interface ReportFactory {

    ValidationReport createValidationReport();

    Message createMessage(Message.Type type, String messageKey,
        Object... context);
    
    public ValidationReport getValidationReport();

}
