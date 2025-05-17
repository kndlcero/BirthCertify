package com.birthcertify.controllers.admin;

import com.birthcertify.models.UserDetails;
import com.birthcertify.services.AdminUserService;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AdminUsersController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUsersController.class);

    @FXML private TableView<UserDetails> usersTable;
    @FXML private TableColumn<UserDetails, String> userIdColumn;
    @FXML private TableColumn<UserDetails, String> nameColumn;
    @FXML private TableColumn<UserDetails, String> emailColumn;
    @FXML private TableColumn<UserDetails, Integer> appCountColumn;
    @FXML private TextField searchField;
    @FXML private ProgressIndicator loadingIndicator;

    private final AdminUserService userService = new AdminUserService();
    private ObservableList<UserDetails> usersList;
    private FilteredList<UserDetails> filteredUsers;

    @FXML
    public void initialize() {
        logger.info("Initializing Admin Users Controller");
        setupColumns();
        setupSearch();
        loadUsers();
    }

    private void setupColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(cell -> cell.getValue().fullNameProperty());
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        appCountColumn.setCellValueFactory(new PropertyValueFactory<>("applicationCount"));

        TableColumn<UserDetails, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                deleteBtn.getStyleClass().add("delete-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        usersTable.getColumns().add(actionCol);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredUsers != null) {
                filteredUsers.setPredicate(user -> {
                    String filter = newVal.toLowerCase();
                    return user.getUserId().toLowerCase().contains(filter) ||
                           user.getEmail().toLowerCase().contains(filter) ||
                           user.getFullName().toLowerCase().contains(filter);
                });
                logger.debug("Search filter applied: {}", newVal);
            }
        });
    }

    private void loadUsers() {
        loadingIndicator.setVisible(true);
        new Thread(() -> {
            try {
                logger.info("Loading users from database...");
                usersList = userService.getAllUsers();
                filteredUsers = new FilteredList<>(usersList, p -> true);
                javafx.application.Platform.runLater(() -> {
                    usersTable.setItems(filteredUsers);
                    loadingIndicator.setVisible(false);
                    logger.info("Users loaded and UI updated.");
                });
            } catch (SQLException e) {
                logger.error("Failed to load users", e);
                javafx.application.Platform.runLater(() -> {
                    showError("Failed to load users", e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void handleDelete(UserDetails user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will delete the user and their applications.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    logger.info("Attempting to delete user with ID: {}", user.getUserId());
                    boolean success = userService.deleteUser(user.getUserId());
                    if (success) {
                        usersList.remove(user);
                        logger.info("User deleted successfully: {}", user.getUserId());
                        showSuccess("User deleted.");
                    } else {
                        logger.warn("Failed to delete user: {}", user.getUserId());
                        showError("Failed", "Unable to delete user.");
                    }
                } catch (Exception e) {
                    logger.error("Exception while deleting user: {}", user.getUserId(), e);
                    showError("Error", e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
