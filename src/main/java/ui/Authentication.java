package ui;

import model.User;

import java.util.Optional;

/**
 * Handles registration, login, and sends each user to the menu for their role.
 * Uses the shared AppContext passed in from Main. Contains no SQL.
 */
public class Authentication {

    private final AppContext ctx;

    public Authentication(AppContext ctx) {
        this.ctx = ctx;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=======================================");
            System.out.println(" KONSIGNA: Consignment & Reseller Ledger");
            System.out.println("=======================================");
            System.out.println("[1] Register");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");

            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Enter 1, 2, or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");

        String fullName = ctx.input().readLine("Full name: ");
        if (fullName.isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return;
        }

        String username = ctx.input().readLine("Username: ");
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (ctx.userDao().usernameExists(username)) {
            System.out.println("That username is already taken.");
            return;
        }

        String password = ctx.input().readLine("Password: ");
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("[1] DISTRIBUTOR");
        System.out.println("[2] STAFF");
        System.out.println("[3] RESELLER");
        String role = switch (ctx.input().readLine("Choose: ")) {
            case "1" -> User.ROLE_DISTRIBUTOR;
            case "2" -> User.ROLE_STAFF;
            case "3" -> User.ROLE_RESELLER;
            default -> null;
        };
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        if (ctx.userDao().register(fullName, username, password, role)) {
            System.out.println("Account created! Role: " + role + " | Status: " + User.STATUS_PENDING);
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        String username = ctx.input().readLine("Username: ");
        String password = ctx.input().readLine("Password: ");

        Optional<User> result = ctx.userDao().login(username, password);
        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }

        User user = result.get();

        // Block accounts that an admin has not approved yet.
        if (!User.STATUS_APPROVED.equals(user.status())) {
            showPendingScreen(user);
            return;
        }

        ctx.logDao().logAction(user.userId(), "LOGIN", user.username() + " logged in");
        routeByRole(user);
        ctx.logDao().logAction(user.userId(), "LOGOUT", user.username() + " logged out");
    }

    /** Shown when a user logs in before an admin has approved their account. */
    private void showPendingScreen(User user) {
        System.out.println();
        System.out.println("=======================================");
        System.out.println("   ACCOUNT PENDING APPROVAL");
        System.out.println("=======================================");
        System.out.println("Hi, " + user.fullName() + "!");
        System.out.println("Your " + user.role() + " account has not been approved yet.");
        System.out.println("Please wait for an admin (distributor) to approve it,");
        System.out.println("then log in again.");
        System.out.println("=======================================");
        ctx.input().readLine("Press Enter to go back... ");
    }

    /** Sends the logged-in user to the menu that matches their role. */
    private void routeByRole(User user) {
        switch (user.role()) {
            case User.ROLE_DISTRIBUTOR -> new AdminMenu(ctx, user).show();
            case User.ROLE_STAFF       -> new StaffMenu(ctx, user).show();
            case User.ROLE_RESELLER    -> new ResellerMenu(ctx, user).show();
            default -> System.out.println("Unknown role: " + user.role());
        }
    }
}