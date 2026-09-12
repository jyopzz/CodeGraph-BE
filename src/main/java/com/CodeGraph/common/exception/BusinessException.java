package com.CodeGraph.common.exception;

import com.CodeGraph.common.validation.BusinessValidation;

public class BusinessException extends RuntimeException {

    private final int responseCode;
    private final BusinessValidation businessValidation;

    public BusinessException(
            int responseCode,
            BusinessValidation businessValidation) {

        this.responseCode = responseCode;
        this.businessValidation = businessValidation;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public BusinessValidation getBusinessValidation() {
        return businessValidation;
    }
}