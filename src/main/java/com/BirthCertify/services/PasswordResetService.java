package com.birthcertify.services;

import com.birthcertify.config.SupabaseConfig;
import com.birthcertify.models.PasswordResetRequest;
import com.birthcertify.models.PasswordUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service for password reset functionality
 */
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final SupabaseConfig config;

    public PasswordResetService(SupabaseConfig config) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.config = config;
    }

    /**
     * Send password reset email to the user's email address
     */
    public CompletableFuture<Void> requestPasswordReset(String email) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/recover";
        
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(email);
        
        String json;
        try {
            json = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
            return future;
        }

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        future.completeExceptionally(new IOException(
                                "Password reset request failed: " + response.code() + "\n" + errorBody));
                        return;
                    }
                    
                    logger.info("Password reset email sent to: {}", email);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }
    
    /**
     * Update user's password using a recovery token (from the reset link)
     */
    public CompletableFuture<Void> updatePassword(String recoveryToken, String newPassword) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/user";
        
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setPassword(newPassword);
        
        String json;
        try {
            json = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
            return future;
        }

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("apikey", config.getApiKey())
                .addHeader("Authorization", "Bearer " + recoveryToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        future.completeExceptionally(new IOException(
                                "Password update failed: " + response.code() + "\n" + errorBody));
                        return;
                    }
                    
                    logger.info("Password updated successfully");
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }
}