package com.CodeGraph.common.validation;

public record ValidationInfo(
        String field,
        String code,
        String message,
        ValidationSeverity severity
) {
}