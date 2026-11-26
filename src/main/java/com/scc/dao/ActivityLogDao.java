
package com.scc.dao;

import com.scc.dao.DatabaseConnection;
import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Record an activity
    public void logActivity(Integer userId, String actionType, String description) {

        String sql = """
                INSERT INTO Activity_Logs (user_id, action, timestamp)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId == null) {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(1, userId);
            }

            // Combine action type and description into the existing action column
            pstmt.setString(2, actionType + " | " + description);

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error recording activity: " + e.getMessage());
        }
    }

    // Retrieve all activity logs
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action, timestamp
                FROM Activity_Logs
                ORDER BY log_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapActivityLog(rs));
            }

        } catch (Exception e) {
            System.out.println("Error retrieving activity logs: " + e.getMessage());
        }

        return logs;
    }

    // Retrieve logs by user ID
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action, timestamp
                FROM Activity_Logs
                WHERE user_id = ?
                ORDER BY log_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapActivityLog(rs));
                }
            }

        } catch (Exception e) {
            System.out.println("Error retrieving user activity logs: " + e.getMessage());
        }

        return logs;
    }

    // Convert database row into ActivityLog object
    private ActivityLog mapActivityLog(ResultSet rs) throws Exception {

        return new ActivityLog(
                rs.getInt("log_id"),
                (Integer) rs.getObject("user_id"),
                rs.getString("action"),
                "",
                rs.getString("timestamp")
        );
    }
}
