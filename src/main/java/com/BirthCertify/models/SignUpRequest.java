package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignUpRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("data")
    private UserMetadata data;

    // Constructors
    public SignUpRequest() {
    }

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public SignUpRequest(String email, String password, UserMetadata data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserMetadata getData() {
        return data;
    }

    public void setData(UserMetadata data) {
        this.data = data;
    }
}