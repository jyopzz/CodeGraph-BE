package com.CodeGraph.profile.validation;

import com.CodeGraph.common.validation.BusinessValidationRule;
import com.CodeGraph.common.validation.ValidationInfo;
import com.CodeGraph.common.validation.ValidationSeverity;

public enum ProfileBusinessValidation
        implements BusinessValidationRule {

    PROFILE_NOT_FOUND(
            "profile",
            "PROFILE_NOT_FOUND",
            "Profile not found",
            ValidationSeverity.ERROR
    ),

    PROFILE_ALREADY_EXISTS(
            "profile",
            "PROFILE_ALREADY_EXISTS",
            "Profile already exists",
            ValidationSeverity.ERROR
    ),

    DISPLAY_NAME_REQUIRED(
            "displayName",
            "DISPLAY_NAME_REQUIRED",
            "Display name cannot be blank",
            ValidationSeverity.ERROR
    ),
    DATE_OF_BIRTH_REQUIRED(
            "dateOfBirth",
            "DATE_OF_BIRTH_REQUIRED",
            "Date of birth cannot be blank",
            ValidationSeverity.ERROR
    ),

    INVALID_DATE_OF_BIRTH(
            "dateOfBirth",
            "INVALID_DATE_OF_BIRTH",
            "Date of birth must be in YYYY-MM-DD format",
            ValidationSeverity.ERROR
    ),

    BIO_TOO_SHORT(
            "bio",
            "BIO_TOO_SHORT",
            "Bio is too short",
            ValidationSeverity.WARNING
    ),

    NO_PROFILE_CHANGES(
            "profile",
            "NO_PROFILE_CHANGES",
            "No changes were made to the profile",
            ValidationSeverity.WARNING
    ),

    DISPLAY_NAME_TOO_LONG(
            "displayName",
            "DISPLAY_NAME_TOO_LONG",
            "Display name is too long",
            ValidationSeverity.WARNING
    ),
    GENDER_REQUIRED(
            "gender",
            "GENDER_REQUIRED",
            "Gender cannot be blank",
            ValidationSeverity.ERROR
    ),
    INVALID_GENDER(
            "gender",
            "INVALID_GENDER",
            "Gender must be one of the allowed values",
            ValidationSeverity.ERROR
    ),
    BIO_TOO_LONG(
            "bio",
            "BIO_TOO_LONG",
            "Bio is too long",
            ValidationSeverity.WARNING
    ),
    LOCATION_TOO_LONG(
            "location",
            "LOCATION_TOO_LONG",
            "Location is too long",
            ValidationSeverity.WARNING
    );

    private final ValidationInfo info;

    ProfileBusinessValidation(
            String field,
            String code,
            String message,
            ValidationSeverity severity) {

        this.info = new ValidationInfo(
                field,
                code,
                message,
                severity
        );
    }

    @Override
    public ValidationInfo info() {
        return info;
    }
}