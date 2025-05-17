package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleSignInRequest {
    @JsonProperty("id_token")
    private String idToken;
    
    @JsonProperty("provider")
    private String provider;

    // Constructors
    public GoogleSignInRequest() {
    }

    // Getters and setters
    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
