package com.birthcertify.services;

import com.birthcertify.models.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Manages authentication tokens and user session
 */
public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private static final String TOKEN_PREF_KEY = "auth_tokens";
    private static final String USER_PREF_KEY = "user_data";
    
    private final ObjectMapper objectMapper;
    private final Preferences preferences;
    private final SupabaseAuthService authService;
    
    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    private Map<String, Object> userData;
    private boolean isAuthenticated = false;
    
    public TokenManager(SupabaseAuthService authService) {
        this.objectMapper = new ObjectMapper();
        this.preferences = Preferences.userNodeForPackage(TokenManager.class);
        this.authService = authService;
        loadTokens();
    }
    
    /**
     * Save authentication response and update token state
     */
    public void saveAuthResponse(AuthResponse response) {
        if (response == null || response.getAccessToken() == null) {
            throw new IllegalArgumentException("Invalid auth response");
        }
        
        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
        this.expiresAt = Instant.now().getEpochSecond() + response.getExpiresIn();
        this.isAuthenticated = true;
        
        // Create user data map
        Map<String, Object> userMap = new HashMap<>();
        if (response.getUser() != null) {
            userMap.put("id", response.getUser().getId());
            userMap.put("email", response.getUser().getEmail());
            if (response.getUser().getUserMetadata() != null) {
                userMap.put("fullName", response.getUser().getUserMetadata().getFullName());
            }
        }
        this.userData = userMap;
        
        // Save to preferences
        try {
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", accessToken);
            tokenData.put("refreshToken", refreshToken);
            tokenData.put("expiresAt", expiresAt);
            
            String tokenJson = objectMapper.writeValueAsString(tokenData);
            String userJson = objectMapper.writeValueAsString(userData);
            
            preferences.put(TOKEN_PREF_KEY, tokenJson);
            preferences.put(USER_PREF_KEY, userJson);
            preferences.flush();
            
            logger.info("Tokens saved successfully for user: {}", userData.get("email"));
        } catch (Exception e) {
            logger.error("Failed to save tokens: {}", e.getMessage());
        }
    }
    
    /**
     * Load tokens from preferences
     */
    @SuppressWarnings("unchecked")
    private void loadTokens() {
        try {
            String tokenJson = preferences.get(TOKEN_PREF_KEY, null);
            String userJson = preferences.get(USER_PREF_KEY, null);
            
            if (tokenJson != null && !tokenJson.isEmpty()) {
                Map<String, Object> tokenData = objectMapper.readValue(tokenJson, Map.class);
                
                this.accessToken = (String) tokenData.get("accessToken");
                this.refreshToken = (String) tokenData.get("refreshToken");
                this.expiresAt = ((Number) tokenData.get("expiresAt")).longValue();
                this.isAuthenticated = true;
                
                if (userJson != null && !userJson.isEmpty()) {
                    this.userData = objectMapper.readValue(userJson, Map.class);
                }
                
                logger.info("Tokens loaded successfully for user: {}", 
                    userData != null ? userData.get("email") : "unknown");
            }
        } catch (Exception e) {
            logger.error("Failed to load tokens: {}", e.getMessage());
            clearTokens();
        }
    }
    
    /**
     * Clear all token data
     */
    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.expiresAt = 0;
        this.userData = null;
        this.isAuthenticated = false;
        
        preferences.remove(TOKEN_PREF_KEY);
        preferences.remove(USER_PREF_KEY);
        try {
            preferences.flush();
        } catch (Exception e) {
            logger.error("Failed to clear preferences: {}", e.getMessage());
        }
        
        logger.info("Tokens cleared successfully");
    }
    
    /**
     * Check if current tokens are valid or need refresh
     */
    public boolean isTokenValid() {
        if (accessToken == null || refreshToken == null) {
            return false;
        }
        
        // Add a buffer of 60 seconds to account for network latency
        return Instant.now().getEpochSecond() < (expiresAt - 60);
    }
    
    /**
     * Refresh the access token if needed
     */
    public void refreshTokenIfNeeded() {
        if (!isAuthenticated || isTokenValid()) {
            return;
        }
        
        if (refreshToken == null) {
            logger.warn("Cannot refresh token, no refresh token available");
            clearTokens();
            return;
        }
        
        try {
            logger.info("Refreshing access token");
            AuthResponse response = authService.refreshToken(refreshToken).get();
            saveAuthResponse(response);
            logger.info("Token refreshed successfully");
        } catch (Exception e) {
            logger.error("Failed to refresh token: {}", e.getMessage());
            clearTokens();
        }
    }
    
    /**
     * Get the current access token, refreshing if needed
     */
    public String getAccessToken() {
        refreshTokenIfNeeded();
        return accessToken;
    }
    
    /**
     * Get authorization header value
     */
    public String getAuthorizationHeader() {
        refreshTokenIfNeeded();
        if (accessToken != null) {
            return "Bearer " + accessToken;
        }
        return null;
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        if (!isAuthenticated) {
            return false;
        }
        
        // Also check token validity
        return isTokenValid() || refreshToken != null;
    }
    
    /**
     * Get user data
     */
    public Map<String, Object> getUserData() {
        return userData;
    }
    
    /**
     * Get user ID
     */
    public String getUserId() {
        return userData != null ? (String) userData.get("id") : null;
    }
    
    /**
     * Get user email
     */
    public String getUserEmail() {
        return userData != null ? (String) userData.get("email") : null;
    }
}