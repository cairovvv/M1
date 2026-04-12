package com.mycompany.sportstrainingacademy;

import java.sql.*;

public class FormTeam {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/james paul/Documents/Athletes.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void createTeam(String name, int captainId, String sport, int maxSize) {
        String check = "SELECT FirstName, LastName FROM tbl_Information WHERE AtheleteID = ?";
        String insert = "INSERT INTO tbl_Teams (TeamName, CaptainID, Sport, MaxTeamSize) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement(check);
            ps1.setInt(1, captainId);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                System.out.println("  [!] Athlete ID " + captainId + " not found.");
                return;
            }
            PreparedStatement ps2 = conn.prepareStatement(insert);
            ps2.setString(1, name); ps2.setInt(2, captainId); ps2.setString(3, sport); ps2.setInt(4, maxSize);
            ps2.executeUpdate();
            System.out.println("  Team created with Captain: " + rs.getString("FirstName"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void displayTeamRecords() {
        String sql = "SELECT t.TeamName, i.FirstName || ' ' || i.LastName AS CapName, t.Sport " +
                     "FROM tbl_Teams t LEFT JOIN tbl_Information i ON t.CaptainID = i.AtheleteID";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  Team: %-15s | Captain: %-15s | Sport: %s%n", 
                        rs.getString("TeamName"), rs.getString("CapName"), rs.getString("Sport"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}