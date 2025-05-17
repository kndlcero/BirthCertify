package com.birthcertify.services;

import com.birthcertify.config.SupabaseConfig;
import com.birthcertify.models.PasswordResetRequest;
import com.birthcertify.models.VerifyEmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling email verification functionality with Supabase Auth
 */
public class EmailVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final SupabaseConfig config;
    private final String verificationEndpoint;
    private final String resendEndpoint;

    public EmailVerificationService(SupabaseConfig config) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.config = config;
        this.verificationEndpoint = config.getUrl() + "/auth/v1/verify";
        this.resendEndpoint = config.getUrl() + "/auth/v1/resend";
    }

    /**
     * Sends a verification email to the specified email address
     * 
     * @param email The email address to send verification to
     * @return CompletableFuture that completes when the operation finishes
     * @throws IllegalArgumentException if email is null or empty
     */
    public CompletableFuture<Void> sendVerificationEmail(String email) {
        validateEmail(email);
        
        PasswordResetRequest request = new PasswordResetRequest(email);
        return executeAuthRequest(resendEndpoint, request, 
            "Verification email sent to: {}", email);
    }

    /**
     * Verifies an email address using the provided token
     * 
     * @param token The verification token from the email link
     * @return CompletableFuture that completes when verification is done
     * @throws IllegalArgumentException if token is null or empty
     */
    public CompletableFuture<Void> verifyEmail(String token) {
        validateToken(token);
        
        VerifyEmailRequest request = new VerifyEmailRequest(token);
        return executeAuthRequest(verificationEndpoint, request, 
            "Email verified successfully");
    }

    /**
     * Executes an authentication request to Supabase
     */
    private <T> CompletableFuture<Void> executeAuthRequest(
            String endpoint, 
            T requestBody,
            String successLogMessage, 
            Object... logArgs) {
            
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request request = buildAuthRequest(endpoint, body);
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    handleAuthResponse(response, future, successLogMessage, logArgs);
                }
            });
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }

    private Request buildAuthRequest(String url, RequestBody body) {
        return new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", config.getApiKey())
            .addHeader("Content-Type", "application/json")
            .build();
    }

    private void handleAuthResponse(
            Response response, 
            CompletableFuture<Void> future,
            String successMessage,
            Object... messageArgs) {
            
        try {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? 
                    response.body().string() : "Unknown error";
                future.completeExceptionally(new IOException(
                    "Auth request failed: " + response.code() + " - " + errorBody));
                return;
            }
            
            logger.info(successMessage, messageArgs);
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        } finally {
            response.close();
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
    }
}