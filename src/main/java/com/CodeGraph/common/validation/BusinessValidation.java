package com.CodeGraph.common.validation;

import java.util.ArrayList;
import java.util.List;

public class BusinessValidation {

    private final List<ValidationMessage> errors =
            new ArrayList<>();

    private final List<ValidationMessage> warnings =
            new ArrayList<>();

    public void add(BusinessValidationRule rule) {

        ValidationMessage message =
                new ValidationMessage(
                        rule.field(),
                        rule.code(),
                        rule.message(),
                        rule.severity()
                );

        if (rule.severity() == ValidationSeverity.ERROR) {
            errors.add(message);
        } else {
            warnings.add(message);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<ValidationMessage> getErrors() {
        return errors;
    }

    public List<ValidationMessage> getWarnings() {
        return warnings;
    }
}