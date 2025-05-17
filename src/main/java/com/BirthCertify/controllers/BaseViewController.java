package com.birthcertify.controllers;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BaseViewController {

    private static final Logger logger = LoggerFactory.getLogger(BaseViewController.class);

    @FXML
    private BorderPane baseBorderPane;

    /**
     * Loads a page FXML from the given folder and page name into the center of the base layout.
     * 
     * @param folder The folder containing the FXML (e.g., "admin", "registrant")
     * @param pageName The name of the FXML file without extension
     */
    public void loadPage(String folder, String pageName) {
        try {
            String fxmlPath = "src/main/resources/fxml" + folder + "/" + pageName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pageRoot = loader.load();

            // Set the loaded page into the center of BorderPane
            baseBorderPane.setCenter(pageRoot);

            logger.info("Loaded page: " + fxmlPath);
        } catch (IOException e) {
            logger.error("Failed to load page: " + folder + "/" + pageName, e);
            showAlert("Load Error", "Failed to load the requested page: " + pageName);
        }
    }

    /**
     * Show an alert dialog with the given title and message.
     * 
     * @param title The title of the alert dialog
     * @param message The content message of the alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

