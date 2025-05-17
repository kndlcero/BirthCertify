package com.birthcertify.controllers.auth;

import com.birthcertify.application.Main;
import com.birthcertify.config.GoogleOAuthHelper;
import com.birthcertify.models.UserMetadata;
import com.birthcertify.services.SupabaseAuthService;
import com.birthcertify.services.TokenManager;
import com.birthcertify.utils.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final SupabaseAuthService authService;
    private final TokenManager tokenManager;

    @FXML private VBox loginContainer;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button googleLoginButton;
    @FXML private Button registerLink;
    @FXML private Label errorLabel;

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public LoginController(SupabaseAuthService authService, TokenManager tokenManager) {
        this.authService = authService;
        this.tokenManager = tokenManager;
    }

    @FXML
    public void initialize() {
        showLoginView();
    }

    private void showLoginView() {
        loginContainer.setVisible(true);
        clearErrors();
    }

    private void clearErrors() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter both email and password");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        login(email, password, new AuthCallback() {
            @Override
            public void onSuccess() {
                logger.info("Login successful, transitioning to main view");
                Platform.runLater(() -> {
                    try {
                        Main.setMainRoot();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showLoginError("Failed to load dashboard");
                    }
                });
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> {
                    showLoginError(message);
                    loginButton.setDisable(false);
                });
            }
        });
    }

    @FXML
    private void handleGoogleLogin() {
        googleLoginButton.setDisable(true);
        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                String idToken = GoogleOAuthHelper.getIdToken();

                if (idToken == null || idToken.isEmpty()) {
                    throw new RuntimeException("Failed to retrieve Google ID token.");
                }

                loginWithGoogle(idToken, new AuthCallback() {
                    @Override
                    public void onSuccess() {
                        logger.info("Google login successful, transitioning to main view");
                        Platform.runLater(() -> {
                            try {
                                Main.setMainRoot();
                            } catch (IOException e) {
                                e.printStackTrace();
                                showLoginError("Failed to load dashboard");
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Platform.runLater(() -> {
                            showLoginError(message);
                            googleLoginButton.setDisable(false);
                        });
                    }
                });

            } catch (Exception e) {
                logger.error("Google OAuth failed", e);
                Platform.runLater(() -> {
                    showLoginError("Google login failed: " + e.getMessage());
                    googleLoginButton.setDisable(false);
                });
            }
        }).start();
    }

    private void showLoginError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void switchToRegister() {
        try {
            Main.setRegisterRoot();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Unable to open registration.");
            errorLabel.setVisible(true);
        }
    }

    // Core login logic
    public void login(String email, String password, AuthCallback callback) {
        authService.signIn(new com.birthcertify.models.SignInRequest(email, password))
            .thenAccept(response -> {
                tokenManager.saveAuthResponse(response);
                initializeSession(response.getUser().getId(), response.getUser().getUserMetadata(), response.getUser().getEmail());
                callback.onSuccess();
            })
            .exceptionally(ex -> {
                logger.error("Login failed: {}", ex.getMessage());
                callback.onError("Login failed: " + ex.getMessage());
                return null;
            });
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        authService.signInWithGoogle(idToken)
            .thenAccept(response -> {
                tokenManager.saveAuthResponse(response);
                initializeSession(response.getUser().getId(), response.getUser().getUserMetadata(), response.getUser().getEmail());
                callback.onSuccess();
            })
            .exceptionally(ex -> {
                logger.error("Google login failed: {}", ex.getMessage());
                callback.onError("Google login failed: " + ex.getMessage());
                return null;
            });
    }

    private void initializeSession(String userId, UserMetadata metadata, String email) {
        String fullName = metadata != null ? metadata.getFullName() : "";
        String firstName = "";
        String lastName = "";

        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+", 2);
            firstName = nameParts[0];
            if (nameParts.length > 1) {
                lastName = nameParts[1];
            }
        }

        UserSession.getInstance().startSession(
            userId,
            "registrant",
            firstName,
            lastName,
            email
        );
    }
}
