package com.birthcertify.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("email_confirmed_at")
    private Date emailConfirmedAt;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("last_sign_in_at")
    private Date lastSignInAt;
    
    @JsonProperty("created_at")
    private Date createdAt;
    
    @JsonProperty("updated_at")
    private Date updatedAt;
    
    @JsonProperty("user_metadata")
    private UserMetadata userMetadata;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getEmailConfirmedAt() {
        return emailConfirmedAt;
    }

    public void setEmailConfirmedAt(Date emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getLastSignInAt() {
        return lastSignInAt;
    }

    public void setLastSignInAt(Date lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserMetadata getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(UserMetadata userMetadata) {
        this.userMetadata = userMetadata;
    }
}