package com.CodeGraph.common.validation;

public interface BusinessValidator<T> {

    BusinessValidation validate(
            BusinessAction action,
            T existing,
            Object request);
}