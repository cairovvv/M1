package com.mycompany.sportstrainingacademy;

import java.util.Scanner;


public class SportsTrainingAcademy {

    private static final String ROLE_ADMIN   = "Admin";
    private static final String ROLE_ATHLETE = "Athlete";

 
    public static void main(String[] args) {
        RegisterAthlete  db       = new RegisterAthlete();
        AuthenticateUser auth     = new AuthenticateUser(db);
        EnrollAthlete    enroller = new EnrollAthlete();
        FormTeam         team     = new FormTeam();
        Scanner          sc       = new Scanner(System.in);

        enroller.initTables();

        while (true) {
            printHeader("SPORTS TRAINING ACADEMY");
            System.out.println("  1. Login");
            System.out.println("  2. Sign Up");
            System.out.println("  3. Exit System");
            System.out.print("\n  Selection: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleRoleSelection(auth, db, enroller, team, sc, false);
                case "2" -> handleRoleSelection(auth, db, enroller, team, sc, true);
                case "3" -> {
                    System.out.println("\n  Goodbye! See you at the academy.");
                    return;
                }
                default  -> System.out.println("\n  [!] Invalid choice.");
            }
        }
    }

   
    private static void handleRoleSelection(AuthenticateUser auth, RegisterAthlete db,
                                             EnrollAthlete enr, FormTeam team,
                                             Scanner sc, boolean isSignUp) {
        System.out.println("\n  Are you an:");
        System.out.println("  1. Athlete");
        System.out.println("  2. Admin");
        System.out.print("\n  Selection: ");
        String roleChoice = sc.nextLine().trim();

        String selectedRole = switch (roleChoice) {
            case "1" -> ROLE_ATHLETE;
            case "2" -> ROLE_ADMIN;
            default  -> null;
        };

        if (selectedRole == null) {
            System.out.println("\n  [!] Invalid role selection.");
            return;
        }

        if (isSignUp) {
            if (selectedRole.equals(ROLE_ADMIN)) {
                System.out.println("\n  [!] Admin accounts cannot be created here.");
            } else {
                handleRegistration(auth, sc);
            }
        } else {
            handleLogin(auth, enr, team, sc, selectedRole);
        }
    }


    private static void handleLogin(AuthenticateUser auth, EnrollAthlete enr,
                                     FormTeam team, Scanner sc, String selectedRole) {
        printHeader("LOGIN");
        System.out.print("  Username: "); String user = sc.nextLine().trim();
        System.out.print("  Password: "); String pass = sc.nextLine().trim();

        String result = auth.authenticate(user, pass, selectedRole);

        switch (result) {
            case AuthenticateUser.AUTH_NOT_FOUND     -> System.out.println("\n  [!] Account does not exist.");
            case AuthenticateUser.AUTH_WRONG_PASS    -> System.out.println("\n  [!] Incorrect password.");
            case AuthenticateUser.AUTH_ROLE_MISMATCH -> System.out.println("\n  [!] Role mismatch. Please select the correct role.");
            case AuthenticateUser.AUTH_ERROR         -> System.out.println("\n  [!] Database error. Try again later.");
            default -> {
                System.out.println("\n  Welcome, " + user + "!");
                handleDashboard(result, enr, team, sc, user);
            }
        }
    }

   
    private static void handleRegistration(AuthenticateUser auth, Scanner sc) {
        printHeader("SIGN UP");
        try {
            System.out.print("  Username   : "); String u   = sc.nextLine().trim();
            System.out.print("  Password   : "); String p   = sc.nextLine().trim();
            System.out.print("  First Name : "); String f   = sc.nextLine().trim();
            System.out.print("  Last Name  : "); String l   = sc.nextLine().trim();
            System.out.print("  Age        : "); int    age = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Sport      : "); String s   = sc.nextLine().trim();
            System.out.print("  BMI        : "); double bmi = Double.parseDouble(sc.nextLine().trim());

            boolean ok = auth.registerAthlete(u, p, f, l, age, s, bmi);
            if (!ok) System.out.println("\n  [!] Registration failed. Username may already exist.");

        } catch (Exception e) {
            System.out.println("\n  [!] Invalid input. Registration cancelled.");
        }
    }


    private static void handleDashboard(String role, EnrollAthlete enr,
                                         FormTeam team, Scanner sc, String username) {
        RegisterAthlete db = new RegisterAthlete();

        while (true) {
            printHeader("DASHBOARD [" + role.toUpperCase() + "]");
            System.out.println("  1. View Roster");
            System.out.println("  2. Enroll in Session");
            if (role.equalsIgnoreCase(ROLE_ADMIN)) System.out.println("  3. Admin Tools");
            if (role.equalsIgnoreCase(ROLE_ATHLETE)) System.out.println("  3. My Profile");
            System.out.println("  4. Logout");
            System.out.print("\n  Selection: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleViewRoster(db);
                case "2" -> handleEnrollment(enr, sc, username);
                case "3" -> {
                    if (role.equalsIgnoreCase(ROLE_ADMIN))        handleAdminTools(team, enr, sc);
                    else if (role.equalsIgnoreCase(ROLE_ATHLETE)) db.displayMyProfile(username);
                    else System.out.println("\n  [!] Access denied.");
                }
                case "4" -> { return; }
                default  -> System.out.println("\n  [!] Invalid choice.");
            }
        }
    }


    private static void handleViewRoster(RegisterAthlete db) {
        printHeader("ATHLETE ROSTER");
        var athletes = db.getAllAthletes();
        if (athletes.isEmpty()) System.out.println("  No athletes registered yet.");
        else athletes.forEach(a -> System.out.println("  " + a));
    }

    
    private static void handleEnrollment(EnrollAthlete enr, Scanner sc, String customerName) {
        printHeader("ENROLL IN SESSION");

        enr.displayAvailableSessions();

        try {
            System.out.print("\n  Enter Athlete ID  : "); int aId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter Session ID  : "); int sId = Integer.parseInt(sc.nextLine().trim());

            double fee = enr.getSessionFee(sId);
            if (fee < 0) {
                System.out.println("\n  [!] Session not found.");
                return;
            }

            System.out.println("\n  ============================================");
            System.out.printf ("  Session Fee : P%.2f%n", fee);
            System.out.println("  This amount will be subject to discount");
            System.out.println("  and 12%% VAT based on your payment method.");
            System.out.println("  ============================================");
            System.out.print("\n  Proceed to payment? (Y/N): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
                System.out.println("\n  Enrollment cancelled.");
                return;
            }

            System.out.println("\n  Select Payment Method:");
            System.out.println("  1. Credit Card  (10% discount)");
            System.out.println("  2. Cash         ( 5% discount)");
            System.out.println("  3. E-Wallet     (15% discount)");
            System.out.print("\n  Selection: ");
            String pmChoice = sc.nextLine().trim();

            PaymentFramework payment = buildPayment(pmChoice, customerName, fee, sc);
            if (payment == null) {
                System.out.println("\n  [!] Invalid payment method. Enrollment cancelled.");
                return;
            }

            String result = enr.processEnrollment(aId, sId, payment);
            System.out.println("\n  " + result);

        } catch (NumberFormatException e) {
            System.out.println("\n  [!] Please enter valid numeric values.");
        }
    }

    
    private static PaymentFramework buildPayment(String choice, String customerName,
                                                  double fee, Scanner sc) {
        return switch (choice) {
            case "1" -> {
                System.out.print("  Credit Balance  : P");
                double bal = Double.parseDouble(sc.nextLine().trim());
                yield new CreditCardPayment(customerName, fee, bal);
            }
            case "2" -> {
                System.out.print("  Cash on Hand    : P");
                double cash = Double.parseDouble(sc.nextLine().trim());
                yield new CashPayment(customerName, fee, cash);
            }
            case "3" -> {
                System.out.print("  Wallet Provider : "); String provider = sc.nextLine().trim();
                System.out.print("  Wallet Balance  : P"); double wBal = Double.parseDouble(sc.nextLine().trim());
                yield new EWalletPayment(customerName, fee, wBal, provider);
            }
            default -> null;
        };
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
                        System.out.print("  Team Name  : "); String name  = sc.nextLine().trim();
                        System.out.print("  Captain ID : "); int    capId = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("  Sport      : "); String sport = sc.nextLine().trim();
                        System.out.print("  Max Size   : "); int    size  = Integer.parseInt(sc.nextLine().trim());
                        team.createTeam(name, capId, sport, size);
                    } catch (Exception e) {
                        System.out.println("\n  [!] Invalid input.");
                    }
                }
                case "2" -> team.displayTeamRecords();
                case "3" -> handleCreateSession(enr, sc);
                case "4" -> { return; }
                default  -> System.out.println("\n  [!] Invalid choice.");
            }
        }
    }

   
    private static void handleCreateSession(EnrollAthlete enr, Scanner sc) {
        printHeader("CREATE SESSION");
        try {
            System.out.print("  Session Name    : "); String name  = sc.nextLine().trim();
            System.out.print("  Sport           : "); String sport = sc.nextLine().trim();
            System.out.print("  Date (YYYY-MM-DD): "); String date = sc.nextLine().trim();
            System.out.print("  Time (HH:MM AM/PM): "); String time = sc.nextLine().trim();
            System.out.print("  Fee (e.g. 500.00): P"); double fee = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Max Capacity    : "); int    cap  = Integer.parseInt(sc.nextLine().trim());

            boolean created = enr.createSession(name, sport, date, time, fee, "Scheduled", cap);
            System.out.println(created
                    ? "\n  Session \"" + name + "\" created successfully."
                    : "\n  [!] Failed to create session.");

        } catch (Exception e) {
            System.out.println("\n  [!] Invalid input. Session not created.");
        }
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(44) + "\n  " + title + "\n" + "=".repeat(44));
    }
}
