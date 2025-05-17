package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMetadata {
    @JsonProperty("full_name")
    private String fullName;

    // Add more custom fields as needed

    // Constructors
    public UserMetadata() {
    }

    public UserMetadata(String fullName) {
        this.fullName = fullName;
    }

    // Getters and setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}