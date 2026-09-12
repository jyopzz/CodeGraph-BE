package com.CodeGraph.profile.controller;

import com.CodeGraph.auth.security.AuthenticatedUser;
import com.CodeGraph.common.response.BaseResponse;
import com.CodeGraph.profile.dto.CreateProfileRequest;
import com.CodeGraph.profile.dto.ProfileResponse;
import com.CodeGraph.profile.dto.UpdateProfileRequest;
import com.CodeGraph.profile.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@Tag(
        name = "Profile",
        description = "APIs for creating, retrieving and updating user profiles"
)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // -------------------------------------------------------------------------
    // GET PROFILE
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "Get profile",
            description = "Retrieves the profile associated with the given user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profile not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<
            BaseResponse<ProfileResponse>
            > getProfile(

            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        Long userId = user.userId();

        var response =
                profileService.getByUserId(userId);

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }


    // -------------------------------------------------------------------------
    // CREATE PROFILE
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Create profile",
            description = "Creates a new profile for a user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Profile created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Profile creation failed because of a business validation error"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<
            BaseResponse<ProfileResponse>
            > create(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Profile information",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CreateProfileRequest.class
                            ),
                            examples = @ExampleObject(
                                    name = "Create Profile",
                                    value = """
                                            {
                                              "displayName": "Jyothis",
                                              "dateOfBirth": "2000-05-20",
                                              "gender": "MALE",
                                              "bio": "Java and Angular developer",
                                              "location": "Kochi"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateProfileRequest request, Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();


        request.setUserId(user.userId());

        var response =
                profileService.create(request);

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }


    // -------------------------------------------------------------------------
    // UPDATE PROFILE
    // -------------------------------------------------------------------------

    @PatchMapping
    @Operation(
            summary = "Update current user's profile",
            description = """
                    Partially updates the currently authenticated user's profile.

                    Only fields supplied in the request are processed.
                    Fields that are not supplied remain unchanged.
                    Blank string values are treated as null and clear the field.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Profile update failed because of a business validation error"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profile not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<BaseResponse<ProfileResponse>> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update. Unspecified fields remain unchanged.",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = UpdateProfileRequest.class
                            ),
                            examples = @ExampleObject(
                                    name = "Update Profile",
                                    value = """
                                            {
                                              "displayName": "Jyothis",
                                              "dateOfBirth": "2000-05-21",
                                              "gender": "MALE",
                                              "bio": "Java and Angular developer",
                                              "location": "Kochi"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        var response =
                profileService.update(
                        user.userId(),
                        request
                );

        return ResponseEntity
                .status(response.getResponseCode())
                .body(response);
    }
}