package com.birthcertify.controllers.auth;

import com.birthcertify.models.UserMetadata;
import com.birthcertify.application.Main;
import com.birthcertify.models.SignUpRequest;
import com.birthcertify.services.SupabaseAuthService;
import com.birthcertify.services.TokenManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final SupabaseAuthService authService;
    private final TokenManager tokenManager;

    @FXML private VBox registerContainer;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField fullNameField;
    @FXML private Button registerButton;
    @FXML private Button loginLink;
    @FXML private Label regErrorLabel;

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public RegisterController(SupabaseAuthService authService, TokenManager tokenManager) {
        this.authService = authService;
        this.tokenManager = tokenManager;
    }

    @FXML
    public void initialize() {
        showRegisterView();
    }

    private void showRegisterView() {
        registerContainer.setVisible(true);
        clearErrors();
    }

    private void clearErrors() {
        regErrorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText().trim();
        String fullName = fullNameField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showRegisterError("Please fill all fields");
            return;
        }

        if (password.length() < 6) {
            showRegisterError("Password must be at least 6 characters");
            return;
        }

        registerButton.setDisable(true);
        regErrorLabel.setVisible(false);

        register(email, password, fullName, new AuthCallback() {
            @Override
            public void onSuccess() {
                logger.info("Registration successful, transitioning to main view");
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> {
                    showRegisterError(message);
                    registerButton.setDisable(false);
                });
            }
        });
    }

    private void showRegisterError(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.setVisible(true);
    }

    @FXML
    private void switchToLogin() {
        try {
            Main.setLoginRoot();
        } catch (Exception e) {
            e.printStackTrace();
            regErrorLabel.setText("Unable to open login screen.");
            regErrorLabel.setVisible(true);
        }
    }

    // Core registration method

    public void register(String email, String password, String fullName, AuthCallback callback) {
        UserMetadata metadata = new UserMetadata(fullName);
        SignUpRequest request = new SignUpRequest(email, password, metadata);

        authService.signUp(request)
            .thenAccept(response -> {
                tokenManager.saveAuthResponse(response);
                logger.info("User registered successfully: {}", email);
                callback.onSuccess();
            })
            .exceptionally(ex -> {
                logger.error("Registration failed: {}", ex.getMessage());
                callback.onError("Registration failed: " + ex.getMessage());
                return null;
            });
    }
}
