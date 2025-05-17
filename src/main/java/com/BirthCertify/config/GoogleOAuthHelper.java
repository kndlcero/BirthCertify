package com.birthcertify.config;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class GoogleOAuthHelper {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("GOOGLE_CLIENT_SECRET"); // optional for PKCE
    private static final String REDIRECT_URI = dotenv.get("GOOGLE_REDIRECT_URI");
    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getIdToken() throws Exception {
        String state = "secure_random_state";
        String authUrl = AUTH_ENDPOINT +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline" +
                "&state=" + state;

        // Open the browser
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            throw new RuntimeException("Desktop not supported. Open the following URL manually: " + authUrl);
        }

        // Wait for the auth code via redirect
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = createHttpServer(codeFuture);
        server.start();

        String authCode = codeFuture.get(); // blocks until the user logs in
        server.stop(0);

        return exchangeCodeForIdToken(authCode);
    }

    private static HttpServer createHttpServer(CompletableFuture<String> codeFuture) throws IOException {
        URI uri = URI.create(REDIRECT_URI);
        int port = uri.getPort() != -1 ? uri.getPort() : 80;
        String path = uri.getPath();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, new AuthCodeHandler(codeFuture));
        return server;
    }

    static class AuthCodeHandler implements HttpHandler {
        private final CompletableFuture<String> codeFuture;

        public AuthCodeHandler(CompletableFuture<String> codeFuture) {
            this.codeFuture = codeFuture;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String code = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        code = param.substring("code=".length());
                        break;
                    }
                }
            }

            String response = "<html><body><h2>Authentication successful! You may now return to the app.</h2></body></html>";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            if (code != null) {
                codeFuture.complete(code);
            } else {
                codeFuture.completeExceptionally(new RuntimeException("Authorization code not found in redirect."));
            }
        }
    }

    private static String exchangeCodeForIdToken(String code) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET) // required for confidential clients
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_ENDPOINT)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Token exchange failed: " + response.code() + " " + response.message());
            }

            JsonNode json = mapper.readTree(response.body().string());
            return json.get("id_token").asText();
        }
    }
}
