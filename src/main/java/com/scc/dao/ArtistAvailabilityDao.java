
package com.scc.dao;

import com.scc.model.ArtistAvailability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtistAvailabilityDao {

    private static final String[] DAYS = {
            "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    // Retrieve an artist's complete weekly schedule
    public List<ArtistAvailability> getSchedule(int artistId) {

        List<ArtistAvailability> schedule = new ArrayList<>();

        String sql = """
                SELECT availability_id, artist_id, day_of_week,
                       start_time, end_time, is_available
                FROM Artist_Availability
                WHERE artist_id = ?
                ORDER BY CASE day_of_week
                    WHEN 'MONDAY' THEN 1
                    WHEN 'TUESDAY' THEN 2
                    WHEN 'WEDNESDAY' THEN 3
                    WHEN 'THURSDAY' THEN 4
                    WHEN 'FRIDAY' THEN 5
                    WHEN 'SATURDAY' THEN 6
                    WHEN 'SUNDAY' THEN 7
                END
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedule.add(new ArtistAvailability(
                            rs.getInt("availability_id"),
                            rs.getInt("artist_id"),
                            rs.getString("day_of_week"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getInt("is_available") == 1
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving schedule: " + e.getMessage());
        }

        return schedule;
    }

    // Save or update one day's schedule
    public boolean saveSchedule(
            int artistId,
            String day,
            String startTime,
            String endTime,
            boolean available
    ) {

        String sql = """
                INSERT INTO Artist_Availability
                    (artist_id, day_of_week, start_time, end_time, is_available)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(artist_id, day_of_week)
                DO UPDATE SET
                    start_time = excluded.start_time,
                    end_time = excluded.end_time,
                    is_available = excluded.is_available
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, artistId);
            pstmt.setString(2, day.toUpperCase());
            pstmt.setString(3, startTime);
            pstmt.setString(4, endTime);
            pstmt.setInt(5, available ? 1 : 0);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
            return false;
        }
    }

    // Initialize an artist's schedule with seven default days
    public boolean initializeSchedule(int artistId) {

        String sql = """
                INSERT OR IGNORE INTO Artist_Availability
                    (artist_id, day_of_week, start_time, end_time, is_available)
                VALUES (?, ?, NULL, NULL, 0)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (String day : DAYS) {
                pstmt.setInt(1, artistId);
                pstmt.setString(2, day);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            System.out.println("Error initializing schedule: " + e.getMessage());
            return false;
        }
    }
}
