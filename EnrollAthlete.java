package com.mycompany.sportstrainingacademy;

import java.sql.*;

/**
 * EnrollAthlete — Manages training sessions and athlete enrollments.
 *
 * New features:
 *   · displayAvailableSessions() shows date, time, sport, and fee per session.
 *   · getSessionFee() lets the main class show the fee BEFORE asking for payment.
 *   · processEnrollment() writes to tbl_Payments and prints a full
 *     schedule + payment receipt after successful enrollment.
 *   · initTables() auto-creates all required tables on first run.
 */
public class EnrollAthlete {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/james paul/Documents/Athletes.db";
    public static final  String ENROLL_SUCCESS = "Athlete enrolled in session successfully.";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ─────────────────────────────────────────────
    // Table Initialization
    // ─────────────────────────────────────────────

    /**
     * Creates all required tables if they do not yet exist.
     * Called once from SportsTrainingAcademy.main() on startup.
     */
    public void initTables() {
        String[] ddl = {
            // Users
            "CREATE TABLE IF NOT EXISTS tbl_Users ("
            + "  Username TEXT PRIMARY KEY,"
            + "  Password TEXT NOT NULL,"
            + "  Roles    TEXT NOT NULL DEFAULT 'Athlete'"
            + ")",
            // Athlete profiles
            "CREATE TABLE IF NOT EXISTS tbl_Information ("
            + "  AtheleteID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  FirstName  TEXT NOT NULL,"
            + "  LastName   TEXT NOT NULL,"
            + "  Age        INTEGER,"
            + "  Sport      TEXT,"
            + "  BMI        REAL,"
            + "  Username   TEXT"
            + ")",
            // Sessions — now includes Sport, Date, Time, Fee
            "CREATE TABLE IF NOT EXISTS tbl_Sessions ("
            + "  SessionID         INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  SessionName       TEXT    NOT NULL,"
            + "  Sport             TEXT    NOT NULL DEFAULT 'General',"
            + "  SessionDate       TEXT    NOT NULL DEFAULT 'TBD',"
            + "  SessionTime       TEXT    NOT NULL DEFAULT 'TBD',"
            + "  Fee               REAL    NOT NULL DEFAULT 0.0,"
            + "  Status            TEXT    NOT NULL DEFAULT 'Scheduled',"
            + "  MaxCapacity       INTEGER NOT NULL,"
            + "  CurrentEnrollment INTEGER NOT NULL DEFAULT 0"
            + ")",
            // Enrollment records
            "CREATE TABLE IF NOT EXISTS tbl_Enrollments ("
            + "  EnrollmentID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  AtheleteID   INTEGER NOT NULL,"
            + "  SessionID    INTEGER NOT NULL,"
            + "  EnrolledAt   TEXT    DEFAULT (datetime('now','localtime'))"
            + ")",
            // Payment records — NEW
            "CREATE TABLE IF NOT EXISTS tbl_Payments ("
            + "  PaymentID     INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  AtheleteID    INTEGER NOT NULL,"
            + "  SessionID     INTEGER NOT NULL,"
            + "  TransactionID TEXT    NOT NULL,"
            + "  AmountPaid    REAL    NOT NULL,"
            + "  PaymentMethod TEXT    NOT NULL,"
            + "  PaymentStatus TEXT    NOT NULL DEFAULT 'Paid',"
            + "  PaidAt        TEXT    DEFAULT (datetime('now','localtime'))"
            + ")",
            // Teams
            "CREATE TABLE IF NOT EXISTS tbl_Teams ("
            + "  TeamID      INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  TeamName    TEXT NOT NULL,"
            + "  CaptainID   INTEGER,"
            + "  Sport       TEXT,"
            + "  MaxTeamSize INTEGER"
            + ")"
        };

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : ddl) st.execute(sql);
        } catch (SQLException e) {
            System.out.println("  [!] Table initialization error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Session Display
    // ─────────────────────────────────────────────

    /**
     * Displays all available sessions with full schedule and fee details.
     */
    public void displayAvailableSessions() {
        String sql = "SELECT * FROM tbl_Sessions "
                   + "WHERE Status = 'Scheduled' AND CurrentEnrollment < MaxCapacity "
                   + "ORDER BY SessionDate, SessionTime";

        try (Connection conn = getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            System.out.println("\n  ============================================");
            System.out.println("           AVAILABLE SESSIONS               ");
            System.out.println("  ============================================");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("  --------------------------------------------");
                System.out.printf ("  Session ID   : %d%n",    rs.getInt("SessionID"));
                System.out.printf ("  Session Name : %s%n",    rs.getString("SessionName"));
                System.out.printf ("  Sport        : %s%n",    rs.getString("Sport"));
                System.out.printf ("  Date         : %s%n",    rs.getString("SessionDate"));
                System.out.printf ("  Time         : %s%n",    rs.getString("SessionTime"));
                System.out.printf ("  Fee          : P%.2f%n", rs.getDouble("Fee"));
                System.out.printf ("  Slots Left   : %d / %d%n",
                        rs.getInt("MaxCapacity") - rs.getInt("CurrentEnrollment"),
                        rs.getInt("MaxCapacity"));
            }
            System.out.println("  ============================================");
            if (!found) System.out.println("  No sessions available at this time.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Fee & Session Lookup
    // ─────────────────────────────────────────────

    /**
     * Returns the fee for a given session ID.
     * Returns -1 if the session is not found.
     * Used by the main class to display the fee BEFORE prompting for payment.
     */
    public double getSessionFee(int sId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Fee FROM tbl_Sessions WHERE SessionID = ?")) {
            ps.setInt(1, sId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("Fee") : -1;
        } catch (SQLException e) {
            return -1;
        }
    }

    // ─────────────────────────────────────────────
    // Post-Payment: Schedule + Receipt Display
    // ─────────────────────────────────────────────

    private void printSessionSchedule(int sId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM tbl_Sessions WHERE SessionID = ?")) {
            ps.setInt(1, sId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n  ============================================");
                System.out.println("           YOUR SESSION SCHEDULE            ");
                System.out.println("  ============================================");
                System.out.printf ("  Session Name : %s%n", rs.getString("SessionName"));
                System.out.printf ("  Sport        : %s%n", rs.getString("Sport"));
                System.out.printf ("  Date         : %s%n", rs.getString("SessionDate"));
                System.out.printf ("  Time         : %s%n", rs.getString("SessionTime"));
                System.out.printf ("  Status       : %s%n", rs.getString("Status"));
                System.out.println("  ============================================");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printPaymentReceipt(int aId, int sId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM tbl_Payments WHERE AtheleteID = ? AND SessionID = ? "
                   + "ORDER BY PaidAt DESC LIMIT 1")) {
            ps.setInt(1, aId);
            ps.setInt(2, sId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n  ============================================");
                System.out.println("              PAYMENT RECEIPT               ");
                System.out.println("  ============================================");
                System.out.printf ("  Transaction ID : %s%n",   rs.getString("TransactionID"));
                System.out.printf ("  Athlete ID     : %d%n",   rs.getInt("AtheleteID"));
                System.out.printf ("  Payment Method : %s%n",   rs.getString("PaymentMethod"));
                System.out.printf ("  Amount Paid    : P%.2f%n",rs.getDouble("AmountPaid"));
                System.out.printf ("  Payment Status : %s%n",   rs.getString("PaymentStatus"));
                System.out.printf ("  Paid At        : %s%n",   rs.getString("PaidAt"));
                System.out.println("  ============================================");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // Enrollment — with Payment Integration
    // ─────────────────────────────────────────────

    /**
     * Processes enrollment with payment.
     *
     * Flow:
     *   1. Validate session (exists + has open slots).
     *   2. Run PaymentFramework.processInvoice() — aborts if validation fails.
     *   3. Write records to tbl_Enrollments and tbl_Payments.
     *   4. Print session schedule + payment receipt.
     */
    public String processEnrollment(int aId, int sId, PaymentFramework payment) {
        String checkSession  = "SELECT Status, MaxCapacity, CurrentEnrollment "
                             + "FROM tbl_Sessions WHERE SessionID = ?";
        String insertEnroll  = "INSERT INTO tbl_Enrollments (AtheleteID, SessionID, EnrolledAt) VALUES (?, ?, datetime('now','localtime'))";
        String updateSession = "UPDATE tbl_Sessions SET CurrentEnrollment = CurrentEnrollment + 1 "
                             + "WHERE SessionID = ?";
        String insertPayment = "INSERT INTO tbl_Payments "
                             + "(AtheleteID, SessionID, TransactionID, AmountPaid, PaymentMethod, PaymentStatus, PaidAt) "
                             + "VALUES (?, ?, ?, ?, ?, 'Paid', datetime('now','localtime'))";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // ── 1. Validate session ───────────────────
            try (PreparedStatement ps = conn.prepareStatement(checkSession)) {
                ps.setInt(1, sId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next())                                                  return "Session not found.";
                if (rs.getInt("CurrentEnrollment") >= rs.getInt("MaxCapacity")) return "Session is full.";
            }

            // ── 2. Process payment ────────────────────
            payment.processInvoice();
            if (payment.getFinalAmount() <= 0) {
                conn.rollback();
                return "Payment failed. Enrollment cancelled.";
            }

            // ── 3. Write records ──────────────────────
            try (PreparedStatement ps1 = conn.prepareStatement(insertEnroll);
                 PreparedStatement ps2 = conn.prepareStatement(updateSession);
                 PreparedStatement ps3 = conn.prepareStatement(insertPayment)) {

                ps1.setInt(1, aId);    ps1.setInt(2, sId);     ps1.executeUpdate();
                ps2.setInt(1, sId);                            ps2.executeUpdate();
                ps3.setInt(1, aId);    ps3.setInt(2, sId);
                ps3.setString(3, payment.getTransactionID());
                ps3.setDouble(4, payment.getFinalAmount());
                ps3.setString(5, payment.paymentMethod);
                ps3.executeUpdate();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                return "Enrollment failed. Transaction rolled back.";
            }

            // ── 4. Show schedule + receipt ────────────
            printSessionSchedule(sId);
            printPaymentReceipt(aId, sId);

            return ENROLL_SUCCESS;

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /** Admin overload — enrolls without payment (no fee required). */
    public String processEnrollment(int aId, int sId) {
        String checkSession  = "SELECT Status, MaxCapacity, CurrentEnrollment "
                             + "FROM tbl_Sessions WHERE SessionID = ?";
        String insertEnroll  = "INSERT INTO tbl_Enrollments (AtheleteID, SessionID) VALUES (?, ?)";
        String updateSession = "UPDATE tbl_Sessions SET CurrentEnrollment = CurrentEnrollment + 1 "
                             + "WHERE SessionID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(checkSession)) {
                ps.setInt(1, sId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next())                                                  return "Session not found.";
                if (rs.getInt("CurrentEnrollment") >= rs.getInt("MaxCapacity")) return "Session is full.";
            }
            try (PreparedStatement ps1 = conn.prepareStatement(insertEnroll);
                 PreparedStatement ps2 = conn.prepareStatement(updateSession)) {
                ps1.setInt(1, aId); ps1.setInt(2, sId); ps1.executeUpdate();
                ps2.setInt(1, sId);                     ps2.executeUpdate();
                conn.commit();
                return ENROLL_SUCCESS;
            } catch (SQLException e) {
                conn.rollback();
                return "Enrollment failed.";
            }
        } catch (SQLException e) {
            return "Database error.";
        }
    }

    // ─────────────────────────────────────────────
    // Session Creation (Admin)
    // ─────────────────────────────────────────────

    /**
     * Full session creation — used by the updated admin tools.
     */
    public boolean createSession(String name, String sport, String date, String time,
                                  double fee, String status, int maxCap) {
        String sql = "INSERT INTO tbl_Sessions "
                   + "(SessionName, Sport, SessionDate, SessionTime, Fee, Status, MaxCapacity, CurrentEnrollment) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);   pstmt.setString(2, sport);
            pstmt.setString(3, date);   pstmt.setString(4, time);
            pstmt.setDouble(5, fee);    pstmt.setString(6, status);
            pstmt.setInt(7, maxCap);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Backward-compatible overload (no schedule/fee). */
    public boolean createSession(String name, String status, int maxCap) {
        return createSession(name, "General", "TBD", "TBD", 0.0, status, maxCap);
    }
}