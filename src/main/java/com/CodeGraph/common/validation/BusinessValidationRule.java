package com.CodeGraph.common.validation;

public interface BusinessValidationRule {

    ValidationInfo info();

    default String field() {
        return info().field();
    }

    default String code() {
        return info().code();
    }

    default String message() {
        return info().message();
    }

    default ValidationSeverity severity() {
        return info().severity();
    }
}