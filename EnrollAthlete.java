package com.mycompany.sportstrainingacademy;

import java.sql.*;

/**
 * Handles session creation and athlete enrollment operations.
 * Connects to the SQLite database to manage tbl_Sessions and tbl_Enrollments.
 */
public class EnrollAthlete {

    // ==================== CONSTANTS ===================
    private static final String DB_URL = "jdbc:sqlite:C:/Users/josek/OneDrive/Documents/NetBeansProjects/SportsTrainingAcademy/Athletes.db";

    // Enrollment result messages
    public static final String ENROLL_SUCCESS    = "Athlete enrolled in session successfully.";
    public static final String ENROLL_NO_ATHLETE = "Athlete not found.";
    public static final String ENROLL_NO_SESSION = "Session not found.";
    public static final String ENROLL_NOT_SCHED  = "Session is already completed or cancelled.";
    public static final String ENROLL_FULL       = "Session is already at full capacity.";
    public static final String ENROLL_FAILED     = "Enrollment failed. Please try again.";
    public static final String ENROLL_DB_ERROR   = "Database error. Please contact support.";

    // ================= DATABASE HELPER ================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ================== CREATE SESSION ================
    /**
     * Creates a new training session in the database.
     *
     * @param name    Name of the session.
     * @param status  Status of the session (e.g., "Scheduled").
     * @param maxCap  Maximum number of athletes allowed.
     * @return true if the session was created successfully.
     */
    public boolean createSession(String name, String status, int maxCap) {
        String sql = "INSERT INTO tbl_Sessions (SessionName, Status, MaxCapacity, CurrentEnrollment) "
                   + "VALUES (?, ?, ?, 0)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, status);
            pstmt.setInt(3, maxCap);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[CreateSession Error] " + e.getMessage());
            return false;
        }
    }

    // ================ PROCESS ENROLLMENT ==============
    /**
     * Enrolls an athlete into a training session after validating:
     * - Athlete exists
     * - Session exists
     * - Session is "Scheduled"
     * - Session is not at full capacity
     *
     * Uses a transaction to safely update both tbl_Enrollments and tbl_Sessions.
     *
     * @param athleteId ID of the athlete to enroll.
     * @param sessionId ID of the session to enroll into.
     * @return A result message string indicating success or the reason for failure.
     */
    public String processEnrollment(int athleteId, int sessionId) {
        String checkAthlete  = "SELECT AtheleteID FROM tbl_Information WHERE AtheleteID = ?";
        String checkSession  = "SELECT Status, MaxCapacity, CurrentEnrollment FROM tbl_Sessions WHERE SessionID = ?";
        String insertEnroll  = "INSERT INTO tbl_Enrollments (AtheleteID, SessionID) VALUES (?, ?)";
        String updateSession = "UPDATE tbl_Sessions SET CurrentEnrollment = CurrentEnrollment + 1 WHERE SessionID = ?";

        try (Connection conn = getConnection()) {

            // --- Validate: Athlete exists ---
            try (PreparedStatement ps = conn.prepareStatement(checkAthlete)) {
                ps.setInt(1, athleteId);
                if (!ps.executeQuery().next()) return ENROLL_NO_ATHLETE;
            }

            // --- Validate: Session exists and is enrollable ---
            try (PreparedStatement ps = conn.prepareStatement(checkSession)) {
                ps.setInt(1, sessionId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) return ENROLL_NO_SESSION;

                String status     = rs.getString("Status");
                int    maxCap     = rs.getInt("MaxCapacity");
                int    currentEnr = rs.getInt("CurrentEnrollment");

                if (!"Scheduled".equalsIgnoreCase(status)) return ENROLL_NOT_SCHED;
                if (currentEnr >= maxCap)                  return ENROLL_FULL;
            }

            // --- Perform enrollment (transaction) ---
            conn.setAutoCommit(false);

            try (PreparedStatement enrollStmt = conn.prepareStatement(insertEnroll);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSession)) {

                enrollStmt.setInt(1, athleteId);
                enrollStmt.setInt(2, sessionId);
                enrollStmt.executeUpdate();

                updateStmt.setInt(1, sessionId);
                updateStmt.executeUpdate();

                conn.commit();
                return ENROLL_SUCCESS;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[ProcessEnrollment Error] " + e.getMessage());
                return ENROLL_FAILED;
            }

        } catch (SQLException e) {
            System.err.println("[DB Connection Error] " + e.getMessage());
            return ENROLL_DB_ERROR;
        }
    }

    // ============= DISPLAY AVAILABLE SESSIONS =========
    /**
     * Displays all sessions that are Scheduled and not yet full.
     * Called before enrollment so the athlete can see their options.
     */
    public void displayAvailableSessions() {
        String sql = "SELECT SessionID, SessionName, Status, MaxCapacity, CurrentEnrollment "
                   + "FROM tbl_Sessions "
                   + "WHERE Status = 'Scheduled' AND CurrentEnrollment < MaxCapacity";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n======================================= AVAILABLE SESSIONS ==========================================");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.printf("  ID: %-3d | Session: %-20s | Status: %-10s | Slots: %d/%d%n",
                    rs.getInt("SessionID"),
                    rs.getString("SessionName"),
                    rs.getString("Status"),
                    rs.getInt("CurrentEnrollment"),
                    rs.getInt("MaxCapacity")
                );
            }

            if (!hasRecords) System.out.println("  No available sessions at the moment.");
            System.out.println("  =====================================================================================================");

        } catch (SQLException e) {
            System.err.println("[DisplayAvailableSessions Error] " + e.getMessage());
        }
    }
}


