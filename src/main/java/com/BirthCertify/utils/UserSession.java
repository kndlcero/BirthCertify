package com.birthcertify.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSession {
    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private static UserSession instance;

    private String userId;
    private String role = "registrant";  // Default to registrant
    private String firstName;
    private String lastName;
    private String email;
    private String selectedApplicationId; // New field for selected application ID

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
            logger.info("User Session instance created.");
        }
        return instance;
    }

    public void startSession(String userId, String role, String firstName, String lastName, String email) {
        this.userId = userId;
        this.role = role != null ? role : "registrant";  // fallback to "registrant"
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        logger.info("Session started for user: {} ({})", getFullName(), userId);
    }

    public void endSession() {
        logger.info("Session ended for user: {} ({})", getFullName(), userId);
        userId = null;
        role = null;
        firstName = null;
        lastName = null;
        email = null;
        selectedApplicationId = null; // Clear selected application ID on session end
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    // New method to set the selected application ID
    public void setSelectedApplicationId(String applicationId) {
        this.selectedApplicationId = applicationId;
        logger.info("Selected application ID set to: {}", applicationId);
    }

    public String getSelectedApplicationId() {
        return selectedApplicationId;
    }
}
