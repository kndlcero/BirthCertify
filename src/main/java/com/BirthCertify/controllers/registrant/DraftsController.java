package com.birthcertify.controllers.registrant;

import com.birthcertify.models.Application;
import com.birthcertify.services.ApplicationService;
import com.birthcertify.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class DraftsController {

    private static final Logger logger = LoggerFactory.getLogger(DraftsController.class);

    @FXML private TableView<Application> draftsTable;
    @FXML private TableColumn<Application, String> cityColumn;
    @FXML private TableColumn<Application, Integer> copiesColumn;
    @FXML private TableColumn<Application, String> statusColumn;
    @FXML private TableColumn<Application, Void> actionColumn;

    @FXML private Label statusLabel;

    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
        setupTable();
        loadDrafts();
    }

    private void setupTable() {
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("cityOfBirth"));
        copiesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfCopies"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button resumeBtn = new Button("Resume");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, resumeBtn, deleteBtn);

            {
                resumeBtn.setOnAction(e -> handleResume(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadDrafts() {
        try {
            List<Application> drafts = applicationService.getDraftsForUser(UserSession.getInstance().getUserId());
            draftsTable.setItems(FXCollections.observableArrayList(drafts));
            logger.info("Loaded {} drafts for user {}", drafts.size(), UserSession.getInstance().getUserId());
        } catch (SQLException e) {
            logger.error("Failed to load drafts for user {}", UserSession.getInstance().getUserId(), e);
            statusLabel.setText("Failed to load drafts.");
        }
    }

    private void handleResume(Application draft) {
        com.birthcertify.utils.DraftEditContext.getInstance().setActiveDraft(draft);
        try {
            com.birthcertify.application.Main.setMainRoot(); // Load form page manually
            logger.info("Draft with ID {} resumed by user {}", draft.getApplicationId(), UserSession.getInstance().getUserId());
        } catch (Exception e) {
            logger.error("Failed to load form for draft ID {}", draft.getApplicationId(), e);
            statusLabel.setText("Failed to load form.");
        }
    }

    private void handleDelete(Application draft) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this draft?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean deleted = applicationService.deleteApplication(draft.getApplicationId());
                    if (deleted) {
                        draftsTable.getItems().remove(draft);
                        statusLabel.setText("Draft deleted.");
                        logger.info("Draft with ID {} deleted by user {}", draft.getApplicationId(), UserSession.getInstance().getUserId());
                    }
                } catch (SQLException e) {
                    logger.error("Failed to delete draft with ID {} for user {}", draft.getApplicationId(), UserSession.getInstance().getUserId(), e);
                    statusLabel.setText("Failed to delete draft.");
                }
            }
        });
    }
}
