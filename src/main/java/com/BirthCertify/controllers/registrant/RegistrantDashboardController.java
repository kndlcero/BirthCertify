package com.birthcertify.controllers.registrant;

import com.birthcertify.controllers.BaseViewController;
import com.birthcertify.models.Application;
import com.birthcertify.services.ApplicationService;
import com.birthcertify.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class RegistrantDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrantDashboardController.class);

    // UI Components - Statistics
    @FXML private Text applicationCountText;
    @FXML private Text pendingCountText;
    @FXML private Text approvedCountText;

    // UI Components - Recent Applications Table
    @FXML private TableView<Application> recentApplicationsTable;
    @FXML private TableColumn<Application, String> applicationIdColumn;
    @FXML private TableColumn<Application, String> nameColumn;
    @FXML private TableColumn<Application, String> dateSubmittedColumn;
    @FXML private TableColumn<Application, String> statusColumn;
    @FXML private TableColumn<Application, Void> actionsColumn;

    // Services
    private ApplicationService applicationService;
    private BaseViewController baseController;

    // Constructor
    public RegistrantDashboardController() {
        this.applicationService = new ApplicationService();
    }

    public void setBaseController(BaseViewController baseController) {
        this.baseController = baseController;
    }

    @FXML
    private void initialize() {
        try {
            logger.info("Initializing Registrant Dashboard");

            // Get and optionally display the registrant's full name
            String registrantFullName = UserSession.getInstance().getFullName();
            // Example: if you have a label defined
            // registrantNameLabel.setText(registrantFullName);

            // Set up table columns
            applicationIdColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getApplicationId() != null ? cellData.getValue().getApplicationId().toString() : ""
                )
            );

            nameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(getOwnerFullName(cellData.getValue()))
            );

            dateSubmittedColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOwnerDateOfBirth() != null ? cellData.getValue().getOwnerDateOfBirth().toString() : ""
                )
            );

            statusColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : ""
                )
            );

            setupActionsColumn();
            loadUserApplicationData();  // New implementation retained

            logger.info("Registrant Dashboard initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing Registrant Dashboard", e);
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    Application application = getTableView().getItems().get(getIndex());
                    viewApplicationDetails(application);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    private void loadUserApplicationData() {
        try {
            String userId = UserSession.getInstance().getUserId();
            List<Application> userApplications = applicationService.getApplicationsForUser (userId);

            ObservableList<Application> applicationData = FXCollections.observableArrayList(userApplications);
            recentApplicationsTable.setItems(applicationData);

            int totalApplications = userApplications.size();
            int pendingApplications = 0;
            int approvedApplications = 0;

            for (Application app : userApplications) {
                if ("PENDING".equalsIgnoreCase(app.getStatus())) {
                    pendingApplications++;
                } else if ("APPROVED".equalsIgnoreCase(app.getStatus())) {
                    approvedApplications++;
                }
            }

            applicationCountText.setText(String.valueOf(totalApplications));
            pendingCountText.setText(String.valueOf(pendingApplications));
            approvedCountText.setText(String.valueOf(approvedApplications));

            logger.info("Successfully loaded user application data. Total applications: {}", totalApplications);

        } catch         (Exception e) {
            logger.error("Failed to load user application data", e);
            showAlert(Alert.AlertType.WARNING,"Data Loading Error", "Failed to load your application data. Please try again later.");
        }
    }

    // Action handlers (loadApplications, checkStatus, downloadCertificates, newApplication, uploadDocuments, contactSupport)

    @FXML
    private void loadApplications() {
        if (baseController != null) {
            baseController.loadPage("registrant", "applications");
            logger.info("Navigating to applications page");
        }
    }

    @FXML
    private void checkStatus() {
        logger.info("Checking application status");
        loadApplications(); // just navigate for now
    }

    private void submitNewApplication(Application application) {
        try {
            boolean submitted = applicationService.submitApplication(application);
            if (submitted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted successfully.");
                loadUserApplicationData(); // Refresh the data
            } else {
                showAlert(Alert.AlertType.WARNING, "Submission Failed", "Failed to submit the application. Please try again.");
            }
        } catch (SQLException e) {
            logger.error("Error submitting application", e);
            showAlert(Alert.AlertType.ERROR, "Submission Error", "Failed to submit the application. Please try again.");
        }
    }

    @FXML
    private void downloadCertificates() {
        try {
            logger.info("Attempting to download approved certificates");

            String userId = UserSession.getInstance().getUserId();
            List<Application> approvedApplications = applicationService.getApprovedApplications(userId);

            if (approvedApplications.isEmpty()) {
                showAlert(Alert.AlertType.WARNING,"No Certificates", "You don't have any approved certificates to download.");
                return;
            }

            if (baseController != null) {
                baseController.loadPage("registrant", "certificates");
            }

        } catch (Exception e) {
            logger.error("Error downloading certificates", e);
            showAlert(Alert.AlertType.WARNING,"Download Error", "Failed to process certificate downloads. Please try again later.");
        }
    }

    @FXML
    private void newApplication() {
        try {
            logger.info("Starting new application process");
            if (baseController != null) {
                baseController.loadPage("registrant", "newApplication");
            }
        } catch (Exception e) {
            logger.error("Error navigating to new application page", e);
            showAlert(Alert.AlertType.WARNING,"Navigation Error", "Failed to open new application form. Please try again.");
        }
    }

    @FXML
    private void uploadDocuments() {
        try {
            logger.info("Navigating to document upload page");
            if (baseController != null) {
                baseController.loadPage("registrant", "documents");
            }
        } catch (Exception e) {
            logger.error("Error navigating to document upload page", e);
            showAlert(Alert.AlertType.WARNING,"Navigation Error", "Failed to open document upload page. Please try again.");
        }
    }

    @FXML
    private void contactSupport() {
        try {
            logger.info("Opening support contact form");
            if (baseController != null) {
                baseController.loadPage("registrant", "support");
            }
        } catch (Exception e) {
            logger.error("Error navigating to support page", e);
            showAlert(Alert.AlertType.WARNING,"Navigation Error", "Failed to open support contact form. Please try again.");
        }
    }

    private void viewApplicationDetails(Application application) {
        if (application == null) return;
        logger.info("Viewing details for application ID: {}", application.getApplicationId());
        UserSession.getInstance().setSelectedApplicationId(application.getApplicationId().toString());
        if (baseController != null) {
            baseController.loadPage("registrant", "applicationDetails");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }    

    private String getOwnerFullName(Application app) {
        StringBuilder sb = new StringBuilder();
        if (app.getOwnerFirstName() != null) sb.append(app.getOwnerFirstName()).append(" ");
        if (app.getOwnerMiddleName() != null && !app.getOwnerMiddleName().isEmpty())
            sb.append(app.getOwnerMiddleName()).append(" ");
        if (app.getOwnerLastName() != null) sb.append(app.getOwnerLastName());
        return sb.toString().trim();
    }
}
