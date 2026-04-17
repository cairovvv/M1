package com.mycompany.sportstrainingacademy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegisterAthlete {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/james paul/Documents/Athletes.db";
    public static final String LOGIN_NOT_FOUND = "NOT_FOUND", LOGIN_WRONG_PASS = "WRONG_PASS", LOGIN_ERROR = "ERROR";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public String loginValidation(String u, String p) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT Password, Roles FROM tbl_Users WHERE Username = ?")) {
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return LOGIN_NOT_FOUND;
            return rs.getString("Password").equals(p) ? rs.getString("Roles") : LOGIN_WRONG_PASS;
        } catch (SQLException e) { return LOGIN_ERROR; }
    }

    public void registerFullProfile(String u, String p, String f, String l, int age, String s, double bmi) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement ps1 = conn.prepareStatement("INSERT INTO tbl_Users VALUES (?, ?, 'Athlete')");
            ps1.setString(1, u); ps1.setString(2, p); ps1.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement("INSERT INTO tbl_Information (FirstName, LastName, Age, Sport, BMI, Username) VALUES (?,?,?,?,?,?)");
            ps2.setString(1, f); ps2.setString(2, l); ps2.setInt(3, age); ps2.setString(4, s); ps2.setDouble(5, bmi); ps2.setString(6, u); ps2.executeUpdate();
            conn.commit();
            System.out.println("  Athlete Registered!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Athletes> getAllAthletes() {
        List<Athletes> list = new ArrayList<>();
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM tbl_Information")) {
            while (rs.next()) list.add(new Athletes(rs.getInt("AtheleteID"), rs.getString("FirstName"), rs.getString("LastName"), rs.getInt("Age"), rs.getString("Sport"), rs.getDouble("BMI")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
