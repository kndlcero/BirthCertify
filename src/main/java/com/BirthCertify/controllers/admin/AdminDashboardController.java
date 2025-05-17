package com.birthcertify.controllers.admin;

import com.birthcertify.controllers.BaseViewController;
import com.birthcertify.models.Application;
import com.birthcertify.services.ApplicationService;
import com.birthcertify.services.ReportService;
import com.birthcertify.services.UserService;
import com.birthcertify.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    // UI Components - Statistics
    @FXML private Text totalApplicationsText;
    @FXML private Text applicationsTrendText;
    @FXML private Text pendingApprovalText;
    @FXML private Text registeredUsersText;

    // UI Components - Charts
    @FXML private PieChart applicationStatusChart;
    @FXML private BarChart<String, Number> applicationsTimeChart;

    // UI Components - Table & Filters
    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, String> idColumn;
    @FXML private TableColumn<Application, String> applicantNameColumn;
    @FXML private TableColumn<Application, String> submissionDateColumn;
    @FXML private TableColumn<Application, String> applicationStatusColumn;
    @FXML private TableColumn<Application, String> assignedToColumn;
    @FXML private TableColumn<Application, Void> actionColumn;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private TextField searchApplicationField;
    @FXML private Pagination applicationsPagination;

    // Services
    private ApplicationService applicationService;
    private UserService userService;
    private ReportService reportService;
    private BaseViewController baseController;

    // Data
    private ObservableList<Application> allApplications;
    private final int ITEMS_PER_PAGE = 10;

    // Constructor
    public AdminDashboardController() {
        this.applicationService = new ApplicationService();
        this.userService = new UserService();
        this.reportService = new ReportService();
    }

    public void setBaseController(BaseViewController baseController) {
        this.baseController = baseController;
    }

    @FXML
    private void initialize() {
        try {
            logger.info("Initializing Admin Dashboard");

            // Get and optionally display the admin's full name
            String adminFullName = UserSession.getInstance().getFullName();
            // Example: display on a label if defined
            // adminNameLabel.setText(adminFullName);

            setupStatusFilter();
            setupSearch();
            initializeTableColumns();
            setupActionColumn();
            loadApplicationData();
            loadUserCount(); // corrected typo from loadUser  Count()
            setupApplicationStatusChart();
            setupApplicationsOverTimeChart();
            setupPagination();

            logger.info("Admin Dashboard initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing Admin Dashboard", e);
        }
    }

    private void setupStatusFilter() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "All", "DRAFT", "PENDING", "IN REVIEW", "APPROVED", "REJECTED"
        );
        filterStatusComboBox.setItems(statusOptions);
        filterStatusComboBox.setValue("All");

        filterStatusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterApplications();
        });
    }

    private void setupSearch() {
        searchApplicationField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterApplications();
        });
    }

    private void initializeTableColumns() {
        // applicationId as String from UUID
        idColumn.setCellValueFactory(cellData -> {
            Application app = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    app.getApplicationId() != null ? app.getApplicationId().toString() : ""
            );
        });
        // Applicant full name computed as owner full name
        applicantNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getOwnerFullName(cellData.getValue())
        ));
        // submissionDate - using owner's date of birth formatted as string (alternative if no submission date)
        submissionDateColumn.setCellValueFactory(cellData -> {
            Application app = cellData.getValue();
            String dateStr = (app.getOwnerDateOfBirth() != null) ? app.getOwnerDateOfBirth().toString(): "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        applicationStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : ""
        ));
        // AssignedTo is not present in Application; show blank or "N/A"
        assignedToColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("N/A"));
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");

            {
                viewBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    viewApplicationDetails(app);
                });

                approveBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    approveApplication(app);
                });

                rejectBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    rejectApplication(app);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Application app = getTableView().getItems().get(getIndex());
                    if ("PENDING".equalsIgnoreCase(app.getStatus())) {
                        var hbox = new javafx.scene.layout.HBox(5);
                        hbox.getChildren().addAll(viewBtn, approveBtn, rejectBtn);
                        setGraphic(hbox);
                    } else {
                        setGraphic(viewBtn);
                    }
                }
            }
        });
    }

    private void loadApplicationData() {
        try {
            // Fetch all applications
            List<Application> applications = applicationService.getAllApplications();
            allApplications = FXCollections.observableArrayList(applications);

            totalApplicationsText.setText(String.valueOf(applications.size()));

            long pendingCount = applications.stream()
                    .filter(app -> "PENDING".equalsIgnoreCase(app.getStatus()))
                    .count();
            pendingApprovalText.setText(String.valueOf(pendingCount));

            int previousMonthCount = applicationService.getPreviousMonthApplicationCount();
            int currentMonthCount = applicationService.getCurrentMonthApplicationCount();

            if (previousMonthCount > 0) {
                double percentChange = ((double) (currentMonthCount - previousMonthCount) / previousMonthCount) * 100;
                String trend = String.format("%+.1f%% from last month", percentChange);
                applicationsTrendText.setText(trend);
            } else {
                applicationsTrendText.setText("No previous data");
            }

            filterApplications();

            logger.info("Successfully loaded application data. Total applications: {}", applications.size());
        } catch (Exception e) {
            logger.error("Failed to load application data", e);
            showAlert(Alert.AlertType.ERROR, "Data Loading Error", "Failed to load application data. Please check the database connection.");
        }
    }

    private void loadUserCount() {
        try {
            int userCount = userService.getTotalUserCount();
            registeredUsersText.setText(String.valueOf(userCount));
            logger.info("User count loaded: {}", userCount);
        } catch (Exception e) {
            logger.error("Failed to load user count", e);
            registeredUsersText.setText("Error");
        }
    }

    private void setupApplicationStatusChart() {
        try {
            Map<String, Integer> statusCounts = applicationService.getApplicationStatusCounts();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            applicationStatusChart.setData(pieChartData);
            applicationStatusChart.setTitle("Application Status Distribution");
            logger.info("Application status chart initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to set up application status chart", e);
        }
    }

    private void setupApplicationsOverTimeChart() {
        try {
            Map<String, Integer> monthlyData = applicationService.getMonthlyApplicationCounts();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Applications");

            for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            applicationsTimeChart.getData().clear();
            applicationsTimeChart.getData().add(series);
            applicationsTimeChart.setTitle("Monthly Applications");

            logger.info("Applications over time chart initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to set up applications over time chart", e);
        }
    }

    private void setupPagination() {
        int totalPages = (allApplications.size() / ITEMS_PER_PAGE) +
                (allApplications.size() % ITEMS_PER_PAGE == 0 ? 0 : 1);
        applicationsPagination.setPageCount(totalPages);
        applicationsPagination.setCurrentPageIndex(0);

        applicationsPagination.setPageFactory(this::createPage);
    }

    private TableView<Application> createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allApplications.size());

        ObservableList<Application> pageItems = FXCollections.observableArrayList(
                allApplications.subList(fromIndex, toIndex)
        );

        applicationsTable.setItems(pageItems);
        return applicationsTable;
    }

    private void filterApplications() {
        try {
            String statusFilter = filterStatusComboBox.getValue();
            String searchText = searchApplicationField.getText().toLowerCase();

            ObservableList<Application> filteredList = FXCollections.observableArrayList();

            for (Application app : allApplications) {
                boolean statusMatch = "All".equalsIgnoreCase(statusFilter) ||
                        (app.getStatus() != null && app.getStatus().equalsIgnoreCase(statusFilter));

                String fullName = getOwnerFullName(app).toLowerCase();
                String idString = app.getApplicationId() != null ? app.getApplicationId().toString().toLowerCase() : "";

                boolean searchMatch = searchText.isEmpty() ||
                        idString.contains(searchText) ||
                        fullName.contains(searchText);

                if (statusMatch && searchMatch) {
                    filteredList.add(app);
                }
            }

            applicationsTable.setItems(filteredList);

            int totalPages = (filteredList.size() / ITEMS_PER_PAGE) +
                    (filteredList.size() % ITEMS_PER_PAGE == 0 ? 0 : 1);
            applicationsPagination.setPageCount(totalPages);
            applicationsPagination.setCurrentPageIndex(0);

            logger.info("Applications filtered. Showing {} results.", filteredList.size());
        } catch (Exception e) {
            logger.error("Error filtering applications", e);
        }
    }

    private String getOwnerFullName(Application app) {
        StringBuilder sb = new StringBuilder();
        if (app.getOwnerFirstName() != null) sb.append(app.getOwnerFirstName()).append(" ");
        if (app.getOwnerMiddleName() != null && !app.getOwnerMiddleName().isEmpty()) sb.append(app.getOwnerMiddleName()).append(" ");
        if (app.getOwnerLastName() != null) sb.append(app.getOwnerLastName());
        return sb.toString().trim();
    }

    // Action methods

    @FXML
    private void reviewApplications() {
        if (baseController != null) {
            baseController.loadPage("admin", "reviewApplications");
        }
    }

    @FXML
    private void manageUsers() {
        if (baseController != null) {
            baseController.loadPage("admin", "userManagement");
        }
    }

    @FXML
    private void openSystemSettings() {
        if (baseController != null) {
            baseController.loadPage("admin", "systemSettings");
        }
    }

    @FXML
    private void generateReports() {
        if (baseController != null) {
            baseController.loadPage("admin", "reports");
        }
    }

    @FXML
    private void viewSystemLogs() {
        if (baseController != null) {
            baseController.loadPage("admin", "systemLogs");
        }
    }

    @FXML
    private void backupSystem() {
        try {
            logger.info("Initiating system backup");

            boolean success = reportService.performSystemBackup();

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Backup Complete", "System backup completed successfully.");
                logger.info("System backup completed successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Backup Failed", "Failed to complete system backup. Please check the logs.");
                logger.error("System backup failed.");
            }
        } catch (Exception e) {
            logger.error("Error performing system backup", e);
            showAlert(Alert.AlertType.ERROR, "Backup Error", "Failed to perform system backup. Error: " + e.getMessage());
        }
    }

    private void viewApplicationDetails(Application application) {
        if (application == null) return;
        UserSession.getInstance().setSelectedApplicationId(application.getApplicationId().toString());
        if (baseController != null) {
            baseController.loadPage("admin", "applicationDetails");
        }
    }

    private void approveApplication(Application application) {
        if (application == null) return;
        // Approval logic here...
        // Implement as per your service/API
    }

    private void rejectApplication(Application application) {
        if (application == null) return;
        // Rejection logic here...
        // Implement as per your service/API
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
