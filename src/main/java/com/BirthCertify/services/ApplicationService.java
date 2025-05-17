package com.birthcertify.services;

import com.birthcertify.models.Application;
import com.birthcertify.utils.DatabaseConnection;
import com.birthcertify.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);  // Logger initialization

    public boolean submitApplication(Application app) throws SQLException {
        String sql = """
            INSERT INTO birth_applications (
                application_id, applicant_id, number_of_copies,
                owner_last_name, owner_first_name, owner_middle_name,
                date_of_birth, place_of_birth, city_of_birth,
                father_last_name, father_first_name, father_middle_name,
                mother_maiden_name, mother_first_name, mother_middle_name,
                requester_last_name, requester_first_name, requester_middle_initial,
                requester_contact_no, signature_url, submitted_at, status
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDING'
            )
        """;

        logger.info("Submitting application for userId: {}", UserSession.getInstance().getUserId());  // Log the user submitting the application

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, UUID.randomUUID());
            stmt.setString(2, UserSession.getInstance().getUserId());
            stmt.setInt(3, app.getNumberOfCopies());

            stmt.setString(4, app.getOwnerLastName());
            stmt.setString(5, app.getOwnerFirstName());
            stmt.setString(6, app.getOwnerMiddleName());

            stmt.setDate(7, Date.valueOf(app.getOwnerDateOfBirth()));
            stmt.setString(8, app.getOwnerPlaceOfBirth());
            stmt.setString(9, app.getCityOfBirth());

            stmt.setString(10, app.getFatherLastName());
            stmt.setString(11, app.getFatherFirstName());
            stmt.setString(12, app.getFatherMiddleName());

            stmt.setString(13, app.getMotherMaidenName());
            stmt.setString(14, app.getMotherFirstName());
            stmt.setString(15, app.getMotherMiddleName());

            stmt.setString(16, app.getRequesterLastName());
            stmt.setString(17, app.getRequesterFirstName());
            stmt.setString(18, app.getRequesterMiddleInitial());
            stmt.setString(19, app.getRequesterContactNo());

            stmt.setString(20, app.getSignatureUrl());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 1) {
                logger.info("Application submitted successfully for userId: {}", UserSession.getInstance().getUserId());
                return true;
            } else {
                logger.warn("Application submission failed for userId: {}", UserSession.getInstance().getUserId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error submitting application for userId: {}", UserSession.getInstance().getUserId(), e);  // Log error
            throw e;
        }
    }

    public List<Application> getApplicationsForUser(String userId) throws SQLException {
        List<Application> apps = new ArrayList<>();

        String sql = "SELECT * FROM birth_applications WHERE applicant_id = ? ORDER BY submitted_at DESC";

        logger.info("Fetching applications for userId: {}", userId);  // Log the user whose applications are being fetched

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Application app = new Application();
                app.loadFromResultSet(rs); // Assuming this method loads data into the app object
                apps.add(app);
            }
            logger.info("Fetched {} applications for userId: {}", apps.size(), userId);  // Log how many applications were fetched
        } catch (SQLException e) {
            logger.error("Error fetching applications for userId: {}", userId, e);  // Log error
            throw e;
        }

        return apps;
    }

    public boolean deleteApplication(UUID applicationId) throws SQLException {
        String sql = "DELETE FROM birth_applications WHERE application_id = ?";

        logger.info("Attempting to delete application with applicationId: {}", applicationId);  // Log the application deletion

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, applicationId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted application with applicationId: {}", applicationId);  // Log successful deletion
                return true;
            } else {
                logger.warn("No application found to delete with applicationId: {}", applicationId);  // Log warning if nothing was deleted
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting application with applicationId: {}", applicationId, e);  // Log error
            throw e;
        }
    }

    public boolean saveDraft(Application app) throws SQLException {
        String sql = """
            INSERT INTO birth_applications (
                application_id, applicant_id, number_of_copies,
                owner_last_name, owner_first_name, owner_middle_name,
                date_of_birth, place_of_birth, city_of_birth,
                father_last_name, father_first_name, father_middle_name,
                mother_maiden_name, mother_first_name, mother_middle_name,
                requester_last_name, requester_first_name, requester_middle_initial,
                requester_contact_no, signature_url, submitted_at, status
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'DRAFT'
            )
        """;

        logger.info("Saving draft for userId: {}", UserSession.getInstance().getUserId());  // Log draft save

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, UUID.randomUUID());
            stmt.setString(2, UserSession.getInstance().getUserId());
            stmt.setInt(3, app.getNumberOfCopies());

            stmt.setString(4, app.getOwnerLastName());
            stmt.setString(5, app.getOwnerFirstName());
            stmt.setString(6, app.getOwnerMiddleName());
            stmt.setDate(7, Date.valueOf(app.getOwnerDateOfBirth()));
            stmt.setString(8, app.getOwnerPlaceOfBirth());
            stmt.setString(9, app.getCityOfBirth());

            stmt.setString(10, app.getFatherLastName());
            stmt.setString(11, app.getFatherFirstName());
            stmt.setString(12, app.getFatherMiddleName());

            stmt.setString(13, app.getMotherMaidenName());
            stmt.setString(14, app.getMotherFirstName());
            stmt.setString(15, app.getMotherMiddleName());

            stmt.setString(16, app.getRequesterLastName());
            stmt.setString(17, app.getRequesterFirstName());
            stmt.setString(18, app.getRequesterMiddleInitial());
            stmt.setString(19, app.getRequesterContactNo());

            stmt.setString(20, app.getSignatureUrl());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 1) {
                logger.info("Draft saved successfully for userId: {}", UserSession.getInstance().getUserId());
                return true;
            } else {
                logger.warn("Failed to save draft for userId: {}", UserSession.getInstance().getUserId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error saving draft for userId: {}", UserSession.getInstance().getUserId(), e);  // Log error
            throw e;
        }
    }

    public List<Application> getDraftsForUser(String userId) throws SQLException {
        List<Application> drafts = new ArrayList<>();
        String sql = "SELECT * FROM birth_applications WHERE applicant_id = ? AND status = 'DRAFT' ORDER BY submitted_at DESC";

        logger.info("Fetching drafts for userId: {}", userId);  // Log the user whose drafts are being fetched

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Application draft = new Application();
                draft.loadFromResultSet(rs);
                drafts.add(draft);
            }
            logger.info("Fetched {} drafts for userId: {}", drafts.size(), userId);  // Log how many drafts were fetched
        } catch (SQLException e) {
            logger.error("Error fetching drafts for userId: {}", userId, e);  // Log error
            throw e;
        }
        return drafts;
    }

    public int getPreviousMonthApplicationCount() throws SQLException {
        String sql = """
            SELECT COUNT(*) AS count FROM birth_applications
            WHERE MONTH(submitted_at) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH)
            AND YEAR(submitted_at) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error fetching previous month application count", e);
            throw e;
        }
        return 0;
    }

    public int getCurrentMonthApplicationCount() throws SQLException {
        String sql = """
            SELECT COUNT(*) AS count FROM birth_applications
            WHERE MONTH(submitted_at) = MONTH(CURRENT_DATE)
            AND YEAR(submitted_at) = YEAR(CURRENT_DATE)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error fetching current month application count", e);
            throw e;
        }
        return 0;
    }

    public Map<String, Integer> getApplicationStatusCounts() throws SQLException {
        Map<String, Integer> statusCounts = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM birth_applications GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                statusCounts.put(status, count);
            }
        } catch (SQLException e) {
            logger.error("Error fetching application status counts", e);
            throw e;
        }
        return statusCounts;
    }

    public Map<String, Integer> getMonthlyApplicationCounts() throws SQLException {
        Map<String, Integer> monthlyCounts = new HashMap<>();
        String sql = """
            SELECT DATE_FORMAT(submitted_at, '%Y-%m') AS month, COUNT(*) AS count
            FROM birth_applications
            GROUP BY month
            ORDER BY month
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                monthlyCounts.put(month, count);
            }
        } catch (SQLException e) {
            logger.error("Error fetching monthly application counts", e);
            throw e;
        }
        return monthlyCounts;
    }

    public List<Application> getAllApplications() throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM birth_applications ORDER BY submitted_at DESC";
    
        logger.info("Fetching all applications");
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                Application app = new Application();
                app.loadFromResultSet(rs); // Assuming your Application class has this method to populate fields
                applications.add(app);
            }
    
            logger.info("Fetched {} applications", applications.size());
    
        } catch (SQLException e) {
            logger.error("Error fetching all applications", e);
            throw e;
        }
    
        return applications;
    }

    public List<Application> getApprovedApplications(String userId) throws SQLException {
        List<Application> approvedApps = new ArrayList<>();
        String sql = "SELECT * FROM birth_applications WHERE applicant_id = ? AND status = 'APPROVED' ORDER BY submitted_at DESC";
    
        logger.info("Fetching approved applications for userId: {}", userId);
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                Application app = new Application();
                app.loadFromResultSet(rs); // make sure this method is implemented in Application
                approvedApps.add(app);
            }
    
            logger.info("Fetched {} approved applications for userId: {}", approvedApps.size(), userId);
    
        } catch (SQLException e) {
            logger.error("Error fetching approved applications for userId: {}", userId, e);
            throw e;
        }
    
        return approvedApps;
    }
    
    
}
