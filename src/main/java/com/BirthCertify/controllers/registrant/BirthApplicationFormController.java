package com.birthcertify.controllers.registrant;

import com.birthcertify.models.Application;
import com.birthcertify.services.ApplicationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class BirthApplicationFormController {

    private static final Logger logger = LoggerFactory.getLogger(BirthApplicationFormController.class);

    @FXML private TextField copiesField;

    @FXML private TextField ownerLastNameField;
    @FXML private TextField ownerFirstNameField;
    @FXML private TextField ownerMiddleNameField;
    @FXML private DatePicker ownerDOBPicker;
    @FXML private TextField ownerPOBField;
    @FXML private TextField cityOfBirthField;

    @FXML private TextField fatherLastNameField;
    @FXML private TextField fatherFirstNameField;
    @FXML private TextField fatherMiddleNameField;

    @FXML private TextField motherMaidenNameField;
    @FXML private TextField motherFirstNameField;
    @FXML private TextField motherMiddleNameField;

    @FXML private TextField requesterLastNameField;
    @FXML private TextField requesterFirstNameField;
    @FXML private TextField requesterMiddleInitialField;
    @FXML private TextField requesterContactNoField;

    @FXML private Button uploadSignatureButton;
    @FXML private Button draftButton; // New button for saving draft
    @FXML private ImageView signaturePreview;
    @FXML private Label statusLabel;

    private String signatureUrl; // Store bucket-uploaded URL if applicable
    private final ApplicationService appService = new ApplicationService();

    @FXML
    private void initialize() {
        uploadSignatureButton.setOnAction(e -> handleSignatureUpload());

        Application draft = com.birthcertify.utils.DraftEditContext.getInstance().getActiveDraft();
        if (draft != null) {
            populateFormFromDraft(draft);
            com.birthcertify.utils.DraftEditContext.getInstance().clear();
        }
    }

    private void populateFormFromDraft(Application app) {
        copiesField.setText(String.valueOf(app.getNumberOfCopies()));
    
        ownerLastNameField.setText(app.getOwnerLastName());
        ownerFirstNameField.setText(app.getOwnerFirstName());
        ownerMiddleNameField.setText(app.getOwnerMiddleName());
        ownerDOBPicker.setValue(app.getOwnerDateOfBirth());
        ownerPOBField.setText(app.getOwnerPlaceOfBirth());
        cityOfBirthField.setText(app.getCityOfBirth());
    
        fatherLastNameField.setText(app.getFatherLastName());
        fatherFirstNameField.setText(app.getFatherFirstName());
        fatherMiddleNameField.setText(app.getFatherMiddleName());
    
        motherMaidenNameField.setText(app.getMotherMaidenName());
        motherFirstNameField.setText(app.getMotherFirstName());
        motherMiddleNameField.setText(app.getMotherMiddleName());
    
        requesterLastNameField.setText(app.getRequesterLastName());
        requesterFirstNameField.setText(app.getRequesterFirstName());
        requesterMiddleInitialField.setText(app.getRequesterMiddleInitial());
        requesterContactNoField.setText(app.getRequesterContactNo());
    
        if (app.getSignatureUrl() != null && !app.getSignatureUrl().isBlank()) {
            signatureUrl = app.getSignatureUrl();
            signaturePreview.setImage(new javafx.scene.image.Image(signatureUrl));
        }
    }

    private void handleSignatureUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload Signature Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selected = chooser.showOpenDialog(uploadSignatureButton.getScene().getWindow());

        if (selected != null) {
            // TEMP placeholder: in real case, upload to Supabase bucket and get URL
            signatureUrl = selected.toURI().toString(); // OR upload + get actual cloud URL
            signaturePreview.setImage(new javafx.scene.image.Image(signatureUrl));
            logger.info("Signature uploaded: {}", signatureUrl);
        }
    }

    @FXML
    private void handleSubmitApplication() {
        try {
            Application app = collectFormData(); // Reuse common builder
            boolean success = appService.submitApplication(app);
            statusLabel.setText(success ? "Application submitted!" : "Submission failed.");
            if (success) {
                logger.info("Application submitted successfully for user: {}", app.getRequesterFirstName());
            } else {
                logger.error("Application submission failed for user: {}", app.getRequesterFirstName());
            }
        } catch (SQLException e) {
            logger.error("Database error occurred during application submission.", e);
            statusLabel.setText("Database error occurred.");
        } catch (Exception e) {
            logger.error("Error occurred during application submission.", e);
            statusLabel.setText("Invalid input.");
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            Application app = collectFormData(); // Reuse common builder
            boolean success = appService.saveDraft(app);
            statusLabel.setText(success ? "Draft saved!" : "Failed to save draft.");
            if (success) {
                logger.info("Draft saved for user: {}", app.getRequesterFirstName());
            } else {
                logger.error("Failed to save draft for user: {}", app.getRequesterFirstName());
            }
        } catch (Exception e) {
            logger.error("Error saving draft.", e);
            statusLabel.setText("Error saving draft.");
        }
    }

    private Application collectFormData() {
        Application app = new Application();
        app.setNumberOfCopies(Integer.parseInt(copiesField.getText()));
        app.setOwnerLastName(ownerLastNameField.getText());
        app.setOwnerFirstName(ownerFirstNameField.getText());
        app.setOwnerMiddleName(ownerMiddleNameField.getText());
        app.setOwnerDateOfBirth(ownerDOBPicker.getValue());
        app.setOwnerPlaceOfBirth(ownerPOBField.getText());
        app.setCityOfBirth(cityOfBirthField.getText());
        app.setFatherLastName(fatherLastNameField.getText());
        app.setFatherFirstName(fatherFirstNameField.getText());
        app.setFatherMiddleName(fatherMiddleNameField.getText());
        app.setMotherMaidenName(motherMaidenNameField.getText());
        app.setMotherFirstName(motherFirstNameField.getText());
        app.setMotherMiddleName(motherMiddleNameField.getText());
        app.setRequesterLastName(requesterLastNameField.getText());
        app.setRequesterFirstName(requesterFirstNameField.getText());
        app.setRequesterMiddleInitial(requesterMiddleInitialField.getText());
        app.setRequesterContactNo(requesterContactNoField.getText());
        app.setSignatureUrl(signatureUrl);
        return app;
    }
}
