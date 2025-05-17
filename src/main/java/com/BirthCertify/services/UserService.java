package com.birthcertify.services;

import com.birthcertify.models.User;
import com.birthcertify.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Retrieves the total count of users in the system.
     *
     * @return total number of users
     */
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) AS total FROM users";
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt("total");
            }
            logger.info("Total user count retrieved: {}", count);

        } catch (SQLException e) {
            logger.error("Error fetching total user count", e);
        }

        return count;
    }

    /**
     * Retrieves a list of all users.
     *
     * @return List of User objects
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY last_sign_in_at DESC"; // Adjust the query as needed

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setEmail(rs.getString("email"));
                user.setEmailConfirmedAt(rs.getDate("email_confirmed_at"));
                user.setPhone(rs.getString("phone"));
                user.setLastSignInAt(rs.getDate("last_sign_in_at"));
                user.setCreatedAt(rs.getDate("created_at"));
                user.setUpdatedAt(rs.getDate("updated_at"));
                
                // Assuming UserMetadata is another class you have defined
                // You may need to implement logic to populate userMetadata if applicable
                // user.setUser Metadata(...);

                users.add(user);
            }
            logger.info("Fetched {} users from database.", users.size());

        } catch (SQLException e) {
            logger.error("Error fetching users list", e);
        }

        return users;
    }

    // Additional user-related methods can be added here, such as createUser , updateUser , deleteUser , etc.
}
