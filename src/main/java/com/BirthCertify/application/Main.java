package com.birthcertify.application;

import com.birthcertify.config.SupabaseConfig;
import com.birthcertify.controllers.auth.LoginController;
import com.birthcertify.controllers.auth.RegisterController;
import com.birthcertify.services.SupabaseAuthService;
import com.birthcertify.services.TokenManager;
import com.birthcertify.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Stage stage;
    private static SupabaseAuthService authService;
    private static TokenManager tokenManager;

    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("Starting BirthCertify application...");

        try {
            // Load Supabase config from .env
            SupabaseConfig config = new SupabaseConfig();
            authService = new SupabaseAuthService(config);
            tokenManager = new TokenManager(authService);
        } catch (Exception e) {
            logger.error("Failed to initialize Supabase services: {}", e.getMessage(), e);
            throw new RuntimeException("Supabase initialization failed", e);
        }

        stage = primaryStage;
        stage.setTitle("BirthCertify");

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (NullPointerException e) {
            logger.error("Icon image not found at /images/icon.png", e);
        }

        setLoginRoot();  // Start with login view
        stage.show();
        logger.info("Application window shown.");
    }

    /**
     * Loads the login view (Login.fxml) with LoginController
     */
    public static void setLoginRoot() throws IOException {
        logger.debug("Loading login view");

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/auth/login.fxml"));
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == LoginController.class) {
                return new LoginController(authService, tokenManager);
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(loader.load(), 780, 460);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setFullScreen(false);
        centerStage();
        logger.info("Login view loaded.");
    }

    /**
     * Loads the registration view (Register.fxml) with RegisterController
     */
    public static void setRegisterRoot() throws IOException {
        logger.debug("Loading register view");

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/auth/register.fxml"));
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == RegisterController.class) {
                return new RegisterController(authService, tokenManager);
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(loader.load(), 780, 460);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setFullScreen(false);
        centerStage();
        logger.info("Register view loaded.");
    }

    private static void centerStage() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        logger.debug("Stage centered on screen.");
    }


    public static void setMainRoot() throws IOException {
        logger.debug("Loading main (registrant) view.");
        Parent root = loadFXML("registrant/dashboard");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(780);
        stage.setMinHeight(460);
        stage.setMaximized(true);
        logger.info("Main view set.");
    }

    public static void setAdminRoot() throws IOException {
        logger.debug("Loading admin view.");
        Parent root = loadFXML("admin/dashboard");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(780);
        stage.setMinHeight(460);
        stage.setMaximized(true);
        logger.info("Admin view set.");
    }

    private static Parent loadFXML(String fxml) throws IOException {
        logger.debug("Loading FXML file: /fxml/{}.fxml", fxml);
        var location = Main.class.getResource("/fxml/" + fxml + ".fxml");
    
        if (location == null) {
            throw new IOException("FXML file not found: /fxml/" + fxml + ".fxml");
        }
    
        return new FXMLLoader(location).load();
    }
    

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down connection pool...");
            DatabaseConnection.closePool();
        }));

        logger.info("Launching JavaFX application...");
        launch();
    }

    public static void toggleFullScreen() {
        boolean isFullScreen = !stage.isFullScreen();
        stage.setFullScreen(isFullScreen);
        logger.info("Toggled full screen mode: {}", isFullScreen);
    }
}
