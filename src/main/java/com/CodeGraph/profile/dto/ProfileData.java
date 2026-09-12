package com.CodeGraph.profile.dto;

import com.CodeGraph.profile.enums.Gender;
import com.CodeGraph.profile.model.Profile;

import java.time.LocalDate;

public class ProfileData {

    private String displayName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String bio;
    private String location;

    public ProfileData() {
    }

    public ProfileData(
            String displayName,
            LocalDate dateOfBirth,
            Gender gender,
            String bio,
            String location) {

        this.displayName = displayName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bio = bio;
        this.location = location;
    }

    public static ProfileData from(Profile profile) {
        return new ProfileData(
                profile.getDisplayName(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getBio(),
                profile.getLocation()
        );
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getBio() {
        return bio;
    }

    public String getLocation() {
        return location;
    }
}