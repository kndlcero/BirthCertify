package com.birthcertify.services;

import com.birthcertify.models.UserDetails;
import com.birthcertify.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);  // Logger initialization

    public ObservableList<UserDetails> getAllUsers() throws SQLException {
        ObservableList<UserDetails> users = FXCollections.observableArrayList();

        String query = """
            SELECT 
                u.user_id,
                u.first_name,
                u.last_name,
                u.email,
                COUNT(a.application_id) AS application_count
            FROM users u
            LEFT JOIN birth_applications a ON u.user_id = a.applicant_id
            WHERE u.role = 'REGISTRANT'
            GROUP BY u.user_id, u.first_name, u.last_name, u.email
            ORDER BY u.last_name
        """;

        logger.info("Executing query to fetch all registrants: {}", query);  // Log the query execution

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserDetails userDetails = new UserDetails(
                    rs.getString("user_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getInt("application_count")
                );
                users.add(userDetails);

                logger.debug("Fetched user details: {}", userDetails);  // Log user details
            }
        } catch (SQLException e) {
            logger.error("Error fetching users from the database", e);  // Log the error
            throw e;  // Rethrow the exception to be handled elsewhere
        }

        logger.info("Successfully fetched {} users", users.size());  // Log the successful operation
        return users;
    }

    public boolean deleteUser(String userId) throws SQLException {
        String[] queries = {
            "DELETE FROM birth_applications WHERE applicant_id = ?",
            "DELETE FROM profiles WHERE user_id = ?",
            "DELETE FROM users WHERE user_id = ?"
        };

        logger.info("Deleting user with userId: {}", userId);  // Log the user deletion action

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String query : queries) {
                    logger.debug("Executing delete query: {}", query);  // Log each query before execution
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, userId);
                        int rowsAffected = stmt.executeUpdate();
                        logger.debug("Rows affected by query: {} -> {}", query, rowsAffected);  // Log rows affected
                    }
                }
                conn.commit();
                logger.info("Successfully deleted user with userId: {}", userId);  // Log success
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error during user deletion for userId: {}. Transaction rolled back.", userId, e);  // Log the error
                throw e;  // Rethrow the exception to be handled elsewhere
            }
        }
    }
}
