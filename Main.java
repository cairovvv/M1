package com.mycompany.sportstrainingacademy;

import java.util.Scanner;

public class SportsTrainingAcademy {

    private static final String ROLE_ADMIN   = "Admin";
    private static final String ROLE_ATHLETE = "Athlete";

    public static void main(String[] args) {
        RegisterAthlete db   = new RegisterAthlete();
        EnrollAthlete   enroller = new EnrollAthlete();
        FormTeam        team     = new FormTeam();
        Scanner         sc       = new Scanner(System.in);

        while (true) {
            printHeader("SPORTS TRAINING ACADEMY");
            System.out.println("  1. Login");
            System.out.println("  2. Sign Up");
            System.out.println("  3. Exit System");
            System.out.print("\n  Selection: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleRoleSelection(db, enroller, team, sc, false);
                case "2" -> handleRoleSelection(db, enroller, team, sc, true);
                case "3" -> {
                    System.out.println("\n  Goodbye! See you at the academy.");
                    return;
                }
                default  -> System.out.println("\n  [!] Invalid choice.");
            }
        }
    }

    private static void handleRoleSelection(RegisterAthlete db, EnrollAthlete enr,
                                            FormTeam team, Scanner sc, boolean isSignUp) {
        System.out.println("\n  Are you an:");
        System.out.println("  1. Athlete");
        System.out.println("  2. Admin");
        System.out.print("\n  Selection: ");
        String roleChoice = sc.nextLine().trim();

        String selectedRole = roleChoice.equals("1") ? ROLE_ATHLETE : (roleChoice.equals("2") ? ROLE_ADMIN : null);

        if (selectedRole == null) {
            System.out.println("\n  [!] Invalid role selection.");
            return;
        }

        if (isSignUp) {
            if (selectedRole.equals(ROLE_ADMIN)) {
                System.out.println("\n  [!] Admin accounts cannot be created here.");
            } else {
                handleRegistration(db, sc);
            }
        } else {
            handleLogin(db, enr, team, sc, selectedRole);
        }
    }

    private static void handleLogin(RegisterAthlete db, EnrollAthlete enr,
                                    FormTeam team, Scanner sc, String selectedRole) {
        printHeader("LOGIN");
        System.out.print("  Username: "); String user = sc.nextLine().trim();
        System.out.print("  Password: "); String pass = sc.nextLine().trim();

        String dbRole = db.loginValidation(user, pass);

        if (dbRole.equals(RegisterAthlete.LOGIN_NOT_FOUND)) {
            System.out.println("\n  [!] Account does not exist.");
        } else if (dbRole.equals(RegisterAthlete.LOGIN_WRONG_PASS)) {
            System.out.println("\n  [!] Incorrect password.");
        } else if (dbRole.equals(RegisterAthlete.LOGIN_ERROR)) {
            System.out.println("\n  [!] Database error.");
        } else {
            if (!dbRole.equalsIgnoreCase(selectedRole)) {
                System.out.println("\n  [!] Role mismatch.");
                return;
            }
            System.out.println("\n  Welcome, " + user + "!");
            handleDashboard(dbRole, db, enr, team, sc);
        }
    }

    private static void handleDashboard(String role, RegisterAthlete db,
                                        EnrollAthlete enr, FormTeam team, Scanner sc) {
        while (true) {
            printHeader("DASHBOARD [" + role.toUpperCase() + "]");
            System.out.println("  1. View Roster");
            System.out.println("  2. Enroll in Session");
            if (role.equalsIgnoreCase(ROLE_ADMIN)) System.out.println("  3. Admin Tools");
            System.out.println("  4. Logout");
            System.out.print("\n  Selection: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleViewRoster(db);
                case "2" -> handleEnrollment(enr, sc);
                case "3" -> {
                    if (role.equalsIgnoreCase(ROLE_ADMIN)) handleAdminTools(team, enr, sc);
                    else System.out.println("\n  [!] Access denied.");
                }
                case "4" -> { return; }
            }
        }
    }

    private static void handleViewRoster(RegisterAthlete db) {
        printHeader("ATHLETE ROSTER");
        var athletes = db.getAllAthletes();
        if (athletes.isEmpty()) System.out.println("  No athletes found.");
        else athletes.forEach(a -> System.out.println("  " + a.toString()));
    }

    private static void handleEnrollment(EnrollAthlete enr, Scanner sc) {
        printHeader("ENROLL IN SESSION");
        enr.displayAvailableSessions();
        try {
            System.out.print("\n  Athlete ID : "); int aId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Session ID : "); int sId = Integer.parseInt(sc.nextLine().trim());
            System.out.println("\n  " + enr.processEnrollment(aId, sId));
        } catch (Exception e) { System.out.println("\n  [!] Use numeric IDs."); }
    }

    private static void handleAdminTools(FormTeam team, EnrollAthlete enr, Scanner sc) {
        while (true) {
            printHeader("ADMIN TOOLS");
            System.out.println("  1. Create Team");
            System.out.println("  2. View Teams");
            System.out.println("  3. Create Session");
            System.out.println("  4. Back");
            System.out.print("\n  Selection: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    try {
                        System.out.print("  Team Name  : "); String name = sc.nextLine().trim();
                        System.out.print("  Captain ID : "); int capId = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("  Sport      : "); String sport = sc.nextLine().trim();
                        System.out.print("  Max Size   : "); int size = Integer.parseInt(sc.nextLine().trim());
                        team.createTeam(name, capId, sport, size);
                    } catch (Exception e) { System.out.println("\n  [!] Invalid input."); }
                }
                case "2" -> team.displayTeamRecords();
                case "3" -> {
                    try {
                        System.out.print("  Session Name : "); String sName = sc.nextLine().trim();
                        System.out.print("  Capacity     : "); int cap = Integer.parseInt(sc.nextLine().trim());
                        enr.createSession(sName, "Scheduled", cap);
                    } catch (Exception e) { System.out.println("\n  [!] Invalid input."); }
                }
                case "4" -> { return; }
            }
        }
    }

    private static void handleRegistration(RegisterAthlete db, Scanner sc) {
        try {
            System.out.print("  Username   : "); String u = sc.nextLine().trim();
            System.out.print("  Password   : "); String p = sc.nextLine().trim();
            System.out.print("  First Name : "); String f = sc.nextLine().trim();
            System.out.print("  Last Name  : "); String l = sc.nextLine().trim();
            System.out.print("  Age        : "); int age = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Sport      : "); String s = sc.nextLine().trim();
            System.out.print("  BMI        : "); double bmi = Double.parseDouble(sc.nextLine().trim());
            db.registerFullProfile(u, p, f, l, age, s, bmi);
        } catch (Exception e) { System.out.println("\n  [!] Registration failed."); }
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(44) + "\n  " + title + "\n" + "=".repeat(44));
    }
}