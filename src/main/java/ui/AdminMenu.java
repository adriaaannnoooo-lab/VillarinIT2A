package ui;

import dao.ActivityLogDao;
import dao.UserDao;
import model.ActivityLog;
import model.User;

import java.util.List;
import java.util.Scanner;

/**
 * Admin (Distributor) menu: manage users, products and resellers, view reports,
 * and view the system audit trail. Contains no SQL. All database work goes through the DAOs.
 */
public class AdminMenu {

    private static final List<String> VALID_ROLES =
            List.of(User.ROLE_DISTRIBUTOR, User.ROLE_STAFF, User.ROLE_RESELLER);
    private static final List<String> VALID_STATUSES =
            List.of(User.STATUS_APPROVED, User.STATUS_PENDING);

    private final AppContext ctx;
    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final User admin;

    public AdminMenu(AppContext ctx, User admin) {
        this.ctx = ctx;
        this.scanner = ctx.scanner();
        this.userDao = ctx.userDao();
        this.logDao = ctx.logDao();
        this.admin = admin;
    }

    public void show() {
        System.out.println("\nWelcome, " + admin.fullName() + "!");
        System.out.println("Role: " + admin.role() + " | Status: " + admin.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("[1] View all system users");
            System.out.println("[2] View pending registrations");
            System.out.println("[3] Approve user account");
            System.out.println("[4] Update user role and status");
            System.out.println("[5] Manage products");
            System.out.println("[6] Manage resellers");
            System.out.println("[7] Search resellers or products");
            System.out.println("[8] View reports");
            System.out.println("[9] View system audit logs");
            System.out.println("[0] Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> updateUser();
                case "5" -> new ProductMenu(ctx, admin).show();
                case "6" -> new ResellerManagementMenu(ctx, admin).show();
                case "7" -> new LookupMenu(ctx).search();
                case "8" -> new ReportMenu(ctx).show();
                case "9" -> auditLogMenu();
                case "0" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------- Users ----------

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No registered users found.");
            return;
        }
        printUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUser() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            int targetId = readIntInput("Enter User ID to approve (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean inPendingList = pending.stream().anyMatch(u -> u.userId() == targetId);
            if (!inPendingList) {
                System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId)) {
                System.out.println("Success! User ID " + targetId + " has been APPROVED.");
                logDao.logAction(admin.userId(), "USER_APPROVED",
                        "Admin " + admin.userId() + " approved account for User ID " + targetId);
            } else {
                System.out.println("Could not approve User ID " + targetId + ".");
            }
            return;
        }
    }

    /** Admin changes another user's role and status. Enter 0 to cancel at any step. */
    private void updateUser() {
        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        while (true) {
            int targetId = readIntInput("Enter User ID to update (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            if (targetId == admin.userId()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            boolean exists = users.stream().anyMatch(u -> u.userId() == targetId);
            if (!exists) {
                System.out.println("User ID " + targetId + " does not exist. Try again.");
                continue;
            }

            String newRole = readValidRole();
            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();
            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            if (userDao.updateUserRoleAndStatus(targetId, newRole, newStatus)) {
                System.out.println("Success! User ID " + targetId + " is now "
                        + newRole + " with status " + newStatus + ".");
                logDao.logAction(admin.userId(), "USER_UPDATED",
                        "Admin " + admin.userId() + ": User " + targetId + " -> " + newRole + "/" + newStatus);
                printUserTable(userDao.getAllUsers());
            } else {
                System.out.println("Could not update User ID " + targetId + ".");
            }
            return;
        }
    }

    // ---------- Audit logs ----------

    private void auditLogMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("[1] View all system logs");
            System.out.println("[2] Filter logs by User ID");
            System.out.println("[3] Clear all logs");
            System.out.println("[0] Back");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> printLogTable(logDao.getAllLogs());
                case "2" -> filterLogs();
                case "3" -> clearLogs();
                case "0" -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void filterLogs() {
        int userId = readIntInput("Enter User ID to filter (0 to cancel): ");
        if (userId == 0) {
            System.out.println("Filter cancelled.");
            return;
        }
        printLogTable(logDao.getLogsByUserId(userId));
    }

    private void clearLogs() {
        System.out.print("Type YES to permanently delete all logs (anything else cancels): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equals("YES")) {
            System.out.println("Clear logs cancelled.");
            return;
        }
        int removed = logDao.clearLogs();
        if (removed >= 0) {
            System.out.println("Success! " + removed + " log entries cleared.");
            logDao.logAction(admin.userId(), "LOGS_CLEARED",
                    "Admin " + admin.userId() + " cleared " + removed + " log entries");
        }
    }

    // ---------- Input validation wrappers ----------

    /** Keeps asking until a whole number is typed. Letters and symbols never crash it. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /** Keeps asking until a valid role is typed. Returns null if 0 is entered to cancel. */
    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (DISTRIBUTOR / STAFF / RESELLER, 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_ROLES.contains(input)) {
                return input;
            }
            System.out.println("Invalid role. Please enter DISTRIBUTOR, STAFF, or RESELLER.");
        }
    }

    /** Keeps asking until a valid status is typed. Returns null if 0 is entered to cancel. */
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED / PENDING, 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status. Please enter APPROVED or PENDING.");
        }
    }

    // ---------- Table output ----------

    private void printUserTable(List<User> users) {
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(17)
                + "+" + "-".repeat(14) + "+" + "-".repeat(12) + "+";
        String rowFormat = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(rowFormat, u.userId(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }

    /** Aligned audit log table: %-5s | %-8s | %-18s | %-35s | %-20s */
    private void printLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No log entries found.");
            return;
        }
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(10) + "+" + "-".repeat(20)
                + "+" + "-".repeat(37) + "+" + "-".repeat(22) + "+";
        String rowFormat = "| %-5s | %-8s | %-18.18s | %-35.35s | %-20s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "User ID", "Action", "Description", "Timestamp");
        System.out.println(border);
        for (ActivityLog log : logs) {
            System.out.printf(rowFormat, log.logId(), log.userId(), log.action(),
                    log.description(), log.logDatetime());
        }
        System.out.println(border);
        System.out.println("Total: " + logs.size());
    }
}
