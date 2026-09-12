package com.CodeGraph.profile.dto;

import com.CodeGraph.profile.enums.Gender;
import com.CodeGraph.profile.model.Profile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateProfileRequest {

    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotNull
    private String gender;

    @Size(max = 1000)
    private String bio;

    @Size(max = 255)
    private String location;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Profile toProfile() {

        Profile profile = new Profile();

        profile.setUserId(userId);
        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setGender(Gender.valueOf(gender.toUpperCase()));
        profile.setBio(bio);
        profile.setLocation(location);

        return profile;
    }
}