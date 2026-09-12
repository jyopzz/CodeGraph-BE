package com.CodeGraph.profile.dto;

import com.CodeGraph.profile.model.Profile;

public class ProfileResponse {

    private ProfileData profile;

    public ProfileResponse() {
    }

    public ProfileResponse(ProfileData profile) {
        this.profile = profile;
    }

    public ProfileData getProfile() {
        return profile;
    }

    public void setProfile(ProfileData profile) {
        this.profile = profile;
    }

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                ProfileData.from(profile)
        );
    }
}