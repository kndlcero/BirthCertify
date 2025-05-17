package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to update password
 */
public class PasswordUpdateRequest {
    @JsonProperty("password")
    private String password;

    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}