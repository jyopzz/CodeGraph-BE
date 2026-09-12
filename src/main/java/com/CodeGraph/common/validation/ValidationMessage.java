package com.CodeGraph.common.validation;

public class ValidationMessage {

    private String field;
    private String code;
    private String message;
    private ValidationSeverity severity;

    public ValidationMessage() {
    }

    public ValidationMessage(
            String field,
            String code,
            String message,
            ValidationSeverity severity) {

        this.field = field;
        this.code = code;
        this.message = message;
        this.severity = severity;
    }

    public String getField() {
        return field;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }
}