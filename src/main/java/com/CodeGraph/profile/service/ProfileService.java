package com.CodeGraph.profile.service;

import com.CodeGraph.common.response.BaseResponse;
import com.CodeGraph.common.validation.BusinessValidation;
import com.CodeGraph.common.util.ObjectChangeUtil;
import com.CodeGraph.common.validation.BusinessAction;
import com.CodeGraph.profile.dto.CreateProfileRequest;
import com.CodeGraph.profile.dto.ProfileResponse;
import com.CodeGraph.profile.dto.UpdateProfileRequest;
import com.CodeGraph.profile.mapper.ProfileMapper;
import com.CodeGraph.profile.model.Profile;
import com.CodeGraph.profile.validation.ProfileBusinessValidation;
import com.CodeGraph.profile.validation.ProfileValidator;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProfileService {

    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;
    private final ObjectChangeUtil objectChangeUtil;
    public ProfileService(
            ProfileMapper profileMapper,
            ProfileValidator profileValidator,
            ObjectChangeUtil objectChangeUtil) {

        this.profileMapper = profileMapper;
        this.profileValidator = profileValidator;
        this.objectChangeUtil = objectChangeUtil;
    }

    public BaseResponse<ProfileResponse> getByUserId(Long userId) {

        Profile profile =
                profileMapper.findByUserId(userId);

        if (profile == null) {

            BusinessValidation validation =
                    new BusinessValidation();

            validation.add(
                    ProfileBusinessValidation.PROFILE_NOT_FOUND
            );

            return new BaseResponse<>(
                    null,
                    "Profile not found",
                    validation,
                    false,
                    404
            );
        }

        return new BaseResponse<>(
                ProfileResponse.from(profile),
                "Profile retrieved successfully",
                new BusinessValidation(),
                true,
                200
        );
    }

    public BaseResponse<ProfileResponse> create(CreateProfileRequest request) {

        Profile existingProfile =
                profileMapper.findByUserId(request.getUserId());

        BusinessValidation validation =
                profileValidator.validate(
                        BusinessAction.INSERT,
                        existingProfile,
                        request
                );

        if (validation.hasErrors()) {

            return new BaseResponse<>(
                    null,
                    "Profile creation failed",
                    validation,
                    false,
                    400
            );
        }
        Profile profile = request.toProfile();

        profileMapper.save(profile);

        Profile createdProfile =
                profileMapper.findByUserId(profile.getUserId());

        return new BaseResponse<>(
                ProfileResponse.from(createdProfile),
                "Profile created successfully",
                validation,
                true,
                201
        );
    }
    public BaseResponse<ProfileResponse> update(
            Long userId,
            UpdateProfileRequest request) {

        Profile existingProfile =
                profileMapper.findByUserId(userId);

        BusinessValidation validation =
                profileValidator.validate(
                        BusinessAction.UPDATE,
                        existingProfile,
                        request
                );

        if (validation.hasErrors()) {

            return new BaseResponse<>(
                    null,
                    "Profile update failed",
                    validation,
                    false,
                    400
            );
        }

        Map<String, Object> changedFields =
                objectChangeUtil.getChangedFields(
                        existingProfile,
                        request
                );

        // Nothing changed
        if (changedFields.isEmpty()) {

            validation.add(
                    ProfileBusinessValidation.NO_PROFILE_CHANGES
            );

            return new BaseResponse<>(
                    ProfileResponse.from(existingProfile),
                    "No profile changes detected",
                    validation,
                    false,
                    400
            );
        }

        // Update database
        int updatedRows =
                profileMapper.update(
                        userId,
                        changedFields
                );

        // No database row was updated
        if (updatedRows == 0) {

            return new BaseResponse<>(
                    null,
                    "Profile update failed",
                    validation,
                    false,
                    404
            );
        }

        // Get updated data
        Profile updatedProfile =
                profileMapper.findByUserId(userId);

        return new BaseResponse<>(
                ProfileResponse.from(updatedProfile),
                "Profile updated successfully",
                validation,
                true,
                200
        );
    }
}
