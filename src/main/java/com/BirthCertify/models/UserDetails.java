package com.birthcertify.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserDetails {
    private static final Logger logger = LoggerFactory.getLogger(UserDetails.class);

    private final String userId;
    private final String firstName;
    private final String lastName;
    private String email;
        private int applicationCount;
            
                public UserDetails(String userId, String firstName, String lastName, String email, int applicationCount) {
                    this.userId = userId;
                    this.firstName = capitalize(firstName);
                    this.lastName = capitalize(lastName);
                    this.email = email;
                    this.applicationCount = applicationCount;
            
                    // Log user details creation
                    logger.info("Created new user details: ID = {}, Name = {} {}, Email = {}", userId, firstName, lastName, email);
                }
            
                public String getUserId() { return userId; }
                public String getFirstName() { return firstName; }
                public String getLastName() { return lastName; }
                public String getEmail() { return email; }
                public int getApplicationCount() { return applicationCount; }
            
                public String getFullName() {
                    return firstName + " " + lastName;
                }
            
                public StringProperty fullNameProperty() {
                    return new SimpleStringProperty(getFullName());
                }
            
                private String capitalize(String input) {
                    if (input == null || input.isEmpty()) return input;
                    return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
                }
            
                // Logging method to track updates
                public void updateEmail(String newEmail) {
                    logger.debug("Updating email for user {} from {} to {}", userId, email, newEmail);
                    this.email = newEmail;
            }
        
            public void updateApplicationCount(int newApplicationCount) {
                logger.debug("Updating application count for user {} from {} to {}", userId, applicationCount, newApplicationCount);
                this.applicationCount = newApplicationCount;
    }
}
