package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to send password reset email
 */
public class PasswordResetRequest {
    @JsonProperty("email")
    private String email;

    public PasswordResetRequest() {
    }

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}