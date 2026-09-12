package com.CodeGraph.profile.validation;

import com.CodeGraph.common.validation.BusinessValidation;
import com.CodeGraph.common.validation.BusinessAction;
import com.CodeGraph.common.validation.BusinessValidator;
import com.CodeGraph.profile.dto.CreateProfileRequest;
import com.CodeGraph.profile.dto.UpdateProfileRequest;
import com.CodeGraph.profile.enums.Gender;
import com.CodeGraph.profile.model.Profile;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class ProfileValidator
        implements BusinessValidator<Profile> {

    @Override
    public BusinessValidation validate(
            BusinessAction action,
            Profile existing,
            Object request) {

        BusinessValidation validation =
                new BusinessValidation();

        switch (action) {

            case INSERT ->
                    validateInsert(
                            existing,
                            (CreateProfileRequest) request,
                            validation
                    );

            case UPDATE ->
                    validateUpdate(
                            existing,
                            (UpdateProfileRequest) request,
                            validation
                    );

            case DELETE ->
                    validateDelete(
                            existing,
                            validation
                    );
        }

        return validation;
    }

    private void validateInsert(
            Profile existing,
            CreateProfileRequest request,
            BusinessValidation validation) {

        if (existing != null) {
            validation.add(
                    ProfileBusinessValidation.PROFILE_ALREADY_EXISTS
            );
            return;
        }

        // Display name
        if (request.getDisplayName() == null
                || request.getDisplayName().isBlank()) {

            validation.add(
                    ProfileBusinessValidation.DISPLAY_NAME_REQUIRED
            );

        } else if (request.getDisplayName().length() > 100) {

            validation.add(
                    ProfileBusinessValidation.DISPLAY_NAME_TOO_LONG
            );
        }

        // Date of birth
        if (request.getDateOfBirth() == null) {

            validation.add(
                    ProfileBusinessValidation.DATE_OF_BIRTH_REQUIRED
            );

        } else if (!request.getDateOfBirth().isBefore(LocalDate.now())) {

            validation.add(
                    ProfileBusinessValidation.INVALID_DATE_OF_BIRTH
            );
        }

        // Gender - required
        if (request.getGender() == null
                || request.getGender().isBlank()) {

            validation.add(
                    ProfileBusinessValidation.GENDER_REQUIRED
            );

        } else {
            try {
                Gender.valueOf(
                        request.getGender().toUpperCase()
                );

            } catch (IllegalArgumentException e) {

                validation.add(
                        ProfileBusinessValidation.INVALID_GENDER
                );
            }
        }

        // Bio - optional
        if (request.getBio() != null) {

            if (request.getBio().length() < 10) {

                validation.add(
                        ProfileBusinessValidation.BIO_TOO_SHORT
                );

            } else if (request.getBio().length() > 1000) {

                validation.add(
                        ProfileBusinessValidation.BIO_TOO_LONG
                );
            }
        }

        // Location - optional
        if (request.getLocation() != null
                && request.getLocation().length() > 255) {

            validation.add(
                    ProfileBusinessValidation.LOCATION_TOO_LONG
            );
        }
    }

    private void validateUpdate(
            Profile existing,
            UpdateProfileRequest request,
            BusinessValidation validation) {

        if (existing == null) {
            validation.add(
                    ProfileBusinessValidation.PROFILE_NOT_FOUND
            );
            return;
        }

        // Display name
        if (request.getDisplayName() != null) {

            if (request.getDisplayName().isNull()) {

                validation.add(
                        ProfileBusinessValidation.DISPLAY_NAME_REQUIRED
                );

            } else {

                String displayName =
                        request.getDisplayName().toString();

                // Remove JSON quotes
                if (displayName.startsWith("\"")
                        && displayName.endsWith("\"")) {

                    displayName =
                            displayName.substring(
                                    1,
                                    displayName.length() - 1
                            );
                }

                if (displayName.isBlank()) {

                    validation.add(
                            ProfileBusinessValidation.DISPLAY_NAME_REQUIRED
                    );

                } else if (displayName.length() > 100) {

                    validation.add(
                            ProfileBusinessValidation.DISPLAY_NAME_TOO_LONG
                    );
                }
            }
        }

        // Date of birth
        if (request.getDateOfBirth() != null) {

            if (request.getDateOfBirth().isNull()) {

                validation.add(
                        ProfileBusinessValidation.DATE_OF_BIRTH_REQUIRED
                );

            } else {

                String dateOfBirth =
                        request.getDateOfBirth().toString();

                if (dateOfBirth.startsWith("\"")
                        && dateOfBirth.endsWith("\"")) {

                    dateOfBirth =
                            dateOfBirth.substring(
                                    1,
                                    dateOfBirth.length() - 1
                            );
                }

                if (dateOfBirth.isBlank()) {

                    validation.add(
                            ProfileBusinessValidation.DATE_OF_BIRTH_REQUIRED
                    );

                } else {

                    try {

                        LocalDate date =
                                LocalDate.parse(dateOfBirth);

                        if (!date.isBefore(LocalDate.now())) {

                            validation.add(
                                    ProfileBusinessValidation
                                            .INVALID_DATE_OF_BIRTH
                            );
                        }

                    } catch (DateTimeParseException e) {

                        validation.add(
                                ProfileBusinessValidation
                                        .INVALID_DATE_OF_BIRTH
                        );
                    }
                }
            }
        }

        // Gender
        if (request.getGender() != null) {

            if (request.getGender().isNull()) {

                validation.add(
                        ProfileBusinessValidation.GENDER_REQUIRED
                );

            } else {

                String gender =
                        request.getGender().toString();

                if (gender.startsWith("\"")
                        && gender.endsWith("\"")) {

                    gender =
                            gender.substring(
                                    1,
                                    gender.length() - 1
                            );
                }

                if (gender.isBlank()) {

                    validation.add(
                            ProfileBusinessValidation.GENDER_REQUIRED
                    );

                } else {

                    try {

                        Gender.valueOf(
                                gender.toUpperCase()
                        );

                    } catch (IllegalArgumentException e) {

                        validation.add(
                                ProfileBusinessValidation.INVALID_GENDER
                        );
                    }
                }
            }
        }

        // Bio - optional
        if (request.getBio() != null) {

            if (!request.getBio().isNull()) {

                String bio =
                        request.getBio().toString();

                if (bio.startsWith("\"")
                        && bio.endsWith("\"")) {

                    bio =
                            bio.substring(
                                    1,
                                    bio.length() - 1
                            );
                }

                if (bio.length() < 10) {

                    validation.add(
                            ProfileBusinessValidation.BIO_TOO_SHORT
                    );

                } else if (bio.length() > 1000) {

                    validation.add(
                            ProfileBusinessValidation.BIO_TOO_LONG
                    );
                }
            }
        }

        // Location - optional
        if (request.getLocation() != null
                && !request.getLocation().isNull()) {

            String location =
                    request.getLocation().toString();

            if (location.startsWith("\"")
                    && location.endsWith("\"")) {

                location =
                        location.substring(
                                1,
                                location.length() - 1
                        );
            }

            if (location.length() > 255) {

                validation.add(
                        ProfileBusinessValidation.LOCATION_TOO_LONG
                );
            }
        }
    }

    private void validateDelete(
            Profile existing,
            BusinessValidation validation) {

        if (existing == null) {
            validation.add(
                    ProfileBusinessValidation.PROFILE_NOT_FOUND
            );
        }
    }
}