package com.CodeGraph.common.response;

import com.CodeGraph.common.validation.BusinessValidation;
import org.springframework.http.HttpStatus;

public class BaseResponse<T> {

    private T data;
    private String message;
    private BusinessValidation businessValidation;
    private boolean successful;
    private int responseCode;

    public BaseResponse() {
    }

    public BaseResponse(
            T data,
            String message,
            BusinessValidation businessValidation,
            boolean successful,
            int responseCode) {

        this.data = data;
        this.message = message;
        this.businessValidation = businessValidation;
        this.successful = successful;
        this.responseCode = responseCode;
    }

    public static <T> BaseResponse<T> success(T data, String message, int responseCode) {
        return new BaseResponse<>(data, message, null, true, responseCode);
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(data, message, null, true, HttpStatus.OK.value());
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BusinessValidation getBusinessValidation() {
        return businessValidation;
    }

    public void setBusinessValidation(
            BusinessValidation businessValidation) {

        this.businessValidation = businessValidation;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}