package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenRequest {
    @JsonProperty("refresh_token")
    private String refreshToken;

    // Constructors
    public RefreshTokenRequest() {
    }

    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
