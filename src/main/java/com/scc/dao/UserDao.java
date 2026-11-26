package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import com.scc.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean registerUser(
            String name, String username, String password, String role) {

        String normalizedRole = role.toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("CUSTOMER")
                && !normalizedRole.equals("MAKEUP_ARTIST")) {
            System.out.println("Invalid registration role.");
            return false;
        }

        String sql = "INSERT INTO Users "
                + "(name, username, password, role, status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, normalizedRole);

            pstmt.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");

            return true;

        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {

        String sql = "SELECT username, role, status FROM Users "
                + "WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Invalid username or password.");
                    return false;
                }

                String status = rs.getString("status");

                if (!"APPROVED".equalsIgnoreCase(status)) {
                    System.out.println(
                            "Your account is not approved yet."
                    );
                    return false;
                }

                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + username + "!");
                System.out.println("Role: " + rs.getString("role"));

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    public String getUserRole(String username) {

        String sql = "SELECT role FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving user role: " + e.getMessage()
            );
        }

        return "";
    }

    public void viewPendingUsers() {

        String sql = "SELECT user_id, username, role, status "
                + "FROM Users "
                + "WHERE status = 'PENDING' "
                + "AND role != 'ADMIN' "
                + "ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n========== PENDING ACCOUNTS ==========");
            System.out.printf("%-10s %-20s %-18s %-12s%n",
                    "USER ID", "USERNAME", "ROLE", "STATUS");

            System.out.println(
                    "------------------------------------------------------------"
            );

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf("%-10d %-20s %-18s %-12s%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No pending accounts found.");
            }

            System.out.println(
                    "======================================"
            );

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving pending accounts: " + e.getMessage()
            );
        }
    }

    public boolean updateUserStatus(int userId, String newStatus) {

        String normalizedStatus =
                newStatus.toUpperCase(Locale.ROOT);

        if (!normalizedStatus.equals("APPROVED")
                && !normalizedStatus.equals("REJECTED")) {
            System.out.println("Invalid account status.");
            return false;
        }

        String sql = "UPDATE Users SET status = ? "
                + "WHERE user_id = ? "
                + "AND role != 'ADMIN' AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, normalizedStatus);
            pstmt.setInt(2, userId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(
                        "Account " + userId + " was "
                                + normalizedStatus.toLowerCase(Locale.ROOT)
                                + "."
                );
                return true;
            }

            System.out.println(
                    "No matching pending account found."
            );
            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Error updating account: " + e.getMessage()
            );
            return false;
        }
    }

    public void viewAllUsers() {
        String sql = "SELECT user_id, name, username, role, status "
                + "FROM Users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n========== ALL SYSTEM USERS ==========");
            System.out.printf("%-10s %-20s %-20s %-18s %-12s%n",
                    "USER ID", "NAME", "USERNAME", "ROLE", "STATUS");

            System.out.println(
                    "--------------------------------------------------------------------------------"
            );

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf("%-10d %-20s %-20s %-18s %-12s%n",
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No users found.");
            }

            System.out.println("======================================");

        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }

    public void viewMakeupArtists() {
        String sql = "SELECT user_id, name, username, status "
                + "FROM Users WHERE role = 'MAKEUP_ARTIST' "
                + "ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n========== MAKEUP ARTISTS ==========");
            System.out.printf("%-10s %-20s %-20s %-12s%n",
                    "USER ID", "NAME", "USERNAME", "STATUS");

            System.out.println(
                    "------------------------------------------------------------"
            );

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf("%-10d %-20s %-20s %-12s%n",
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No makeup artists found.");
            }

            System.out.println("======================================");

        } catch (SQLException e) {
            System.out.println("Error retrieving makeup artists: " + e.getMessage());
        }
    }

    public void viewCustomerInfo(int customerId) {
        String sql = "SELECT user_id, name, username, status "
                + "FROM Users WHERE user_id = ? AND role = 'CUSTOMER'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n========== CUSTOMER INFORMATION ==========");
                    System.out.println("Customer ID : " + rs.getInt("user_id"));
                    System.out.println("Name        : " + rs.getString("name"));
                    System.out.println("Username    : " + rs.getString("username"));
                    System.out.println("Status      : " + rs.getString("status"));
                    System.out.println("==========================================");
                } else {
                    System.out.println("Customer not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving customer information: " + e.getMessage());
        }
    }

    public int getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting user ID: " + e.getMessage());
        }

        return -1;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, name, username, password, role, status "
                + "FROM Users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, name, username, password, role, status "
                + "FROM Users WHERE status = ? ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "PENDING");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving pending users: " + e.getMessage());
        }

        return users;
    }

    public boolean approveUser(int userId) {
        String sql = "UPDATE Users SET status = ? "
                + "WHERE user_id = ? AND status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "APPROVED");
            pstmt.setInt(2, userId);
            pstmt.setString(3, "PENDING");

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("User account approved successfully!");
                return true;
            } else {
                System.out.println(
                        "No pending account found with User ID: " + userId
                );
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserRoleAndStatus(
            int userId, String newRole, String newStatus) {

        if (newRole == null || newStatus == null) {
            System.out.println("Role and status cannot be empty.");
            return false;
        }

        String normalizedRole = newRole.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = newStatus.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("CUSTOMER")
                && !normalizedRole.equals("MAKEUP_ARTIST")) {
            System.out.println("Invalid user role.");
            return false;
        }

        if (!normalizedStatus.equals("APPROVED")
                && !normalizedStatus.equals("PENDING")) {
            System.out.println("Invalid account status.");
            return false;
        }

        String sql = "UPDATE Users SET role = ?, status = ? "
                + "WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, normalizedRole);
            pstmt.setString(2, normalizedStatus);
            pstmt.setInt(3, userId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("User account updated successfully!");
                return true;
            }

            System.out.println("User ID not found.");
            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Error updating user account: " + e.getMessage()
            );
            return false;
        }
    }



    public boolean deleteUser(int userId, int adminId) {

        String selectSql = "SELECT name, username, role FROM Users WHERE user_id = ?";

        String deleteSql = "DELETE FROM Users WHERE user_id = ? AND role != 'ADMIN'";

        String logSql = "INSERT INTO Activity_Logs "
                + "(user_id, action, timestamp) "
                + "VALUES (?, ?, CURRENT_TIMESTAMP)";

        // Verified dependency queries based on your actual database schema
        String[] dependencyQueries = {
                "SELECT COUNT(*) FROM Artist_Availability WHERE artist_id = ?",
                "SELECT COUNT(*) FROM Appointments WHERE customer_id = ?",
                "SELECT COUNT(*) FROM Appointments WHERE artist_id = ?",
                "SELECT COUNT(*) FROM Activity_Logs WHERE user_id = ?"
        };

        String[] dependencyNames = {
                "Artist Availability",
                "Customer Appointments",
                "Artist Appointments",
                "Activity Logs"
        };

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Check if the target user exists
                String targetName;
                String targetUsername;
                String targetRole;

                try (PreparedStatement selectStmt =
                             conn.prepareStatement(selectSql)) {

                    selectStmt.setInt(1, userId);

                    try (ResultSet rs = selectStmt.executeQuery()) {

                        if (!rs.next()) {
                            System.out.println("User ID not found.");
                            conn.rollback();
                            return false;
                        }

                        targetName = rs.getString("name");
                        targetUsername = rs.getString("username");
                        targetRole = rs.getString("role");
                    }
                }

                // 2. Prevent Admin deletion
                if ("ADMIN".equalsIgnoreCase(targetRole)) {
                    System.out.println(
                            "Admin accounts cannot be deleted through this feature."
                    );
                    conn.rollback();
                    return false;
                }

                // 3. Check foreign-key dependencies
                boolean hasDependencies = false;

                System.out.println("\n=== CHECKING USER RECORDS ===");

                for (int i = 0; i < dependencyQueries.length; i++) {

                    try (PreparedStatement checkStmt =
                                 conn.prepareStatement(dependencyQueries[i])) {

                        checkStmt.setInt(1, userId);

                        try (ResultSet rs = checkStmt.executeQuery()) {

                            if (rs.next()) {
                                int count = rs.getInt(1);

                                if (count > 0) {
                                    hasDependencies = true;

                                    System.out.println(
                                            dependencyNames[i] + ": " + count
                                    );
                                }
                            }
                        }
                    }
                }

                // 4. Stop deletion if related records exist
                if (hasDependencies) {
                    System.out.println(
                            "\nDeletion blocked: This user has related records."
                    );

                    System.out.println(
                            "Resolve the related records first before deleting this account."
                    );

                    conn.rollback();
                    return false;
                }

                // 5. Delete the user account
                try (PreparedStatement deleteStmt =
                             conn.prepareStatement(deleteSql)) {

                    deleteStmt.setInt(1, userId);

                    int deleted = deleteStmt.executeUpdate();

                    if (deleted == 0) {
                        System.out.println("User could not be deleted.");
                        conn.rollback();
                        return false;
                    }
                }

                // 6. Record the deletion in Activity Logs
                String action = "Admin deleted user account"
                        + " | User ID: " + userId
                        + " | Name: " + targetName
                        + " | Username: " + targetUsername
                        + " | Role: " + targetRole
                        + " | Admin ID: " + adminId;

                try (PreparedStatement logStmt =
                             conn.prepareStatement(logSql)) {

                    logStmt.setInt(1, adminId);
                    logStmt.setString(2, action);

                    logStmt.executeUpdate();
                }

                // 7. Commit transaction
                conn.commit();

                System.out.println("User account deleted successfully!");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }


}