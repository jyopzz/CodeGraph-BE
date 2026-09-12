package com.CodeGraph.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

public class UpdateProfileRequest {

    @Schema(
            type = "string",
            example = "Jyothis",
            description = "Display name"
    )
    private JsonNode displayName;

    @Schema(
            type = "string",
            format = "date",
            example = "2000-05-20",
            description = "Date of birth in YYYY-MM-DD format"
    )
    private JsonNode dateOfBirth;

    @Schema(
            type = "string",
            example = "MALE",
            description = "Gender"
    )
    private JsonNode gender;

    @Schema(
            type = "string",
            example = "Java and Angular developer",
            description = "Profile bio"
    )
    private JsonNode bio;

    @Schema(
            type = "string",
            example = "Kochi",
            description = "Location"
    )
    private JsonNode location;

    public JsonNode getDisplayName() {
        return displayName;
    }

    public void setDisplayName(JsonNode displayName) {
        this.displayName = displayName;
    }

    public JsonNode getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(JsonNode dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public JsonNode getGender() {
        return gender;
    }

    public void setGender(JsonNode gender) {
        this.gender = gender;
    }

    public JsonNode getBio() {
        return bio;
    }

    public void setBio(JsonNode bio) {
        this.bio = bio;
    }

    public JsonNode getLocation() {
        return location;
    }

    public void setLocation(JsonNode location) {
        this.location = location;
    }
}