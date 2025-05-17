package com.birthcertify.services;

import com.birthcertify.models.SignUpRequest;
import com.birthcertify.models.SignInRequest;
import com.birthcertify.models.GoogleSignInRequest;
import com.birthcertify.models.AuthResponse;
import com.birthcertify.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;

    @Autowired
    public AuthService(RestTemplate restTemplate, SupabaseConfig supabaseConfig) {
        this.restTemplate = restTemplate;
        this.supabaseConfig = supabaseConfig;
    }

    public AuthResponse signUp(SignUpRequest request) {
        String url = supabaseConfig.getUrl() + "/auth/v1/signup";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getApiKey());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AuthResponse.class
        );
        
        return response.getBody();
    }

    public AuthResponse signIn(SignInRequest request) {
        String url = supabaseConfig.getUrl() + "/auth/v1/token?grant_type=password";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getApiKey());
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AuthResponse.class
        );
        
        return response.getBody();
    }

    public AuthResponse signInWithGoogle(GoogleSignInRequest request) {
        String url = supabaseConfig.getUrl() + "/auth/v1/token?grant_type=id_token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getApiKey());
        
        Map<String, Object> body = new HashMap<>();
        body.put("id_token", request.getIdToken());
        body.put("provider", "google");
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AuthResponse.class
        );
        
        return response.getBody();
    }
}