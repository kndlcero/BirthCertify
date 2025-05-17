package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to verify email
 */
public class VerifyEmailRequest {
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("type")
    private String type;

    public VerifyEmailRequest() {
        this.type = "signup";
    }

    public VerifyEmailRequest(String token) {
        this.token = token;
        this.type = "signup";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}