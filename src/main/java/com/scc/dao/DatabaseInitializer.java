
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {

    public void initializeDatabase() {

        System.out.println(
                "DATABASE LOCATION: " +
                        new java.io.File("makeupsystemdatabase.db").getAbsolutePath()
        );

        String[] createTables = {

                """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS Makeup_Artists (
                    artist_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    artist_name TEXT NOT NULL,
                    specialization TEXT,
                    contact_number TEXT,
                    availability TEXT DEFAULT 'AVAILABLE'
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS Services (
                    service_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    service_name TEXT NOT NULL,
                    price REAL NOT NULL,
                    description TEXT,
                    duration INTEGER DEFAULT 60,
                    status TEXT DEFAULT 'ACTIVE'
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS Appointments (
                    appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    artist_id INTEGER NOT NULL,
                    service_id INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (customer_id) REFERENCES Users(user_id),
                    FOREIGN KEY (artist_id) REFERENCES Users(user_id),
                    FOREIGN KEY (service_id) REFERENCES Services(service_id)
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS Artist_Availability (
                    availability_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    artist_id INTEGER NOT NULL,
                    day_of_week TEXT NOT NULL,
                    start_time TEXT,
                    end_time TEXT,
                    is_available INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (artist_id) REFERENCES Users(user_id)
                        ON DELETE CASCADE,
                    UNIQUE (artist_id, day_of_week)
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS Activity_Logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                )
                """
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables if they do not exist
            for (String sql : createTables) {
                stmt.execute(sql);
            }

            // Upgrade existing Services table if needed
            if (!columnExists(conn, "Services", "duration")) {
                stmt.execute(
                        "ALTER TABLE Services ADD COLUMN duration INTEGER DEFAULT 60"
                );
                System.out.println("Added missing Services.duration column.");
            }

            if (!columnExists(conn, "Services", "status")) {
                stmt.execute(
                        "ALTER TABLE Services ADD COLUMN status TEXT DEFAULT 'ACTIVE'"
                );
                System.out.println("Added missing Services.status column.");
            }

            // Upgrade existing Appointments table if needed
            if (!columnExists(conn, "Appointments", "appointment_time")) {
                stmt.execute(
                        "ALTER TABLE Appointments ADD COLUMN appointment_time TEXT"
                );
                System.out.println("Added missing Appointments.appointment_time column.");
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println(
                    "Database initialization failed: " + e.getMessage()
            );

            throw new IllegalStateException(
                    "Unable to initialize the Glamoura database.", e
            );
        }
    }

    private boolean columnExists(
            Connection conn, String tableName, String columnName
    ) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
