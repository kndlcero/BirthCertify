package com.birthcertify.services;

import com.birthcertify.models.*;
import com.birthcertify.config.SupabaseConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SupabaseAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final SupabaseConfig config;

    public SupabaseAuthService(SupabaseConfig config) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.config = config;
    }

    /**
     * Register a new user with email and password
     */
    public CompletableFuture<AuthResponse> signUp(SignUpRequest request) {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/signup";
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
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                        future.completeExceptionally(new IOException("Unexpected response code: " + response.code() + "\n" + errorBody));
                        return;
                    }

                    if (responseBody == null) {
                        future.completeExceptionally(new IOException("Empty response body"));
                        return;
                    }

                    String responseData = responseBody.string();
                    AuthResponse authResponse = objectMapper.readValue(responseData, AuthResponse.class);
                    future.complete(authResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Sign in with email and password
     */
    public CompletableFuture<AuthResponse> signIn(SignInRequest request) {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/token?grant_type=password";
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
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                        future.completeExceptionally(new IOException("Unexpected response code: " + response.code() + "\n" + errorBody));
                        return;
                    }

                    if (responseBody == null) {
                        future.completeExceptionally(new IOException("Empty response body"));
                        return;
                    }

                    String responseData = responseBody.string();
                    AuthResponse authResponse = objectMapper.readValue(responseData, AuthResponse.class);
                    future.complete(authResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Sign in with Google OAuth
     */
    public CompletableFuture<AuthResponse> signInWithGoogle(String idToken) {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/token?grant_type=id_token";
        
        GoogleSignInRequest request = new GoogleSignInRequest();
        request.setIdToken(idToken);
        request.setProvider("google");
        
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
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                        future.completeExceptionally(new IOException("Unexpected response code: " + response.code() + "\n" + errorBody));
                        return;
                    }

                    if (responseBody == null) {
                        future.completeExceptionally(new IOException("Empty response body"));
                        return;
                    }

                    String responseData = responseBody.string();
                    AuthResponse authResponse = objectMapper.readValue(responseData, AuthResponse.class);
                    future.complete(authResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Refresh the access token using a refresh token
     */
    public CompletableFuture<AuthResponse> refreshToken(String refreshToken) {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/token?grant_type=refresh_token";
        
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        
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
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                        future.completeExceptionally(new IOException("Unexpected response code: " + response.code() + "\n" + errorBody));
                        return;
                    }

                    if (responseBody == null) {
                        future.completeExceptionally(new IOException("Empty response body"));
                        return;
                    }

                    String responseData = responseBody.string();
                    AuthResponse authResponse = objectMapper.readValue(responseData, AuthResponse.class);
                    future.complete(authResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Sign out the current user
     */
    public CompletableFuture<Void> signOut(String accessToken) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        String url = config.getUrl() + "/auth/v1/logout";

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("apikey", config.getApiKey())
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Failed to sign out: " + response.code()));
                } else {
                    future.complete(null);
                }
                response.close();
            }
        });

        return future;
    }
}