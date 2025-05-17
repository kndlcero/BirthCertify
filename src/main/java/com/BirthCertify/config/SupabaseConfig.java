package com.birthcertify.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseConfig.class);
    private final Dotenv dotenv;
    
    public SupabaseConfig() {
        // Load environment variables from .env file
        try {
            this.dotenv = Dotenv.configure().load();
            logger.info("Loaded Supabase configuration from .env file");
        } catch (Exception e) {
            logger.error("Failed to load .env file: {}", e.getMessage());
            throw new RuntimeException("Failed to load .env file", e);
        }
    }

    public String getUrl() {
        String url = dotenv.get("SUPABASE_URL");
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("SUPABASE_URL not found in .env file");
        }
        return url;
    }

    public String getApiKey() {
        String apiKey = dotenv.get("SUPABASE_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("SUPABASE_API_KEY not found in .env file");
        }
        return apiKey;
    }
}