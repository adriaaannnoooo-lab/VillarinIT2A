package ui;

import dao.UserDao;
import model.User;

import java.util.Optional;
import java.util.Scanner;

/**
 * Handles registration, login, and sends each user to the menu for their role.
 * Uses the shared Scanner and UserDao passed in from Main.
 */
public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
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
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
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

        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();
        if (fullName.isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("That username is already taken.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("[1] DISTRIBUTOR");
        System.out.println("[2] STAFF");
        System.out.println("[3] RESELLER");
        System.out.print("Choose: ");
        String role = switch (scanner.nextLine().trim()) {
            case "1" -> User.ROLE_DISTRIBUTOR;
            case "2" -> User.ROLE_STAFF;
            case "3" -> User.ROLE_RESELLER;
            default -> null;
        };
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        if (userDao.register(fullName, username, password, role)) {
            System.out.println("Account created! Role: " + role + " | Status: " + User.STATUS_PENDING);
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> result = userDao.login(username, password);
        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }
        routeByRole(result.get());
    }

    /** Sends the logged-in user to the menu that matches their role. */
    private void routeByRole(User user) {
        switch (user.role()) {
            case User.ROLE_DISTRIBUTOR -> new DistributorMenu(scanner, userDao, user).show();
            case User.ROLE_STAFF       -> new StaffMenu(scanner, userDao, user).show();
            case User.ROLE_RESELLER    -> new ResellerMenu(scanner, userDao, user).show();
            default -> System.out.println("Unknown role: " + user.role());
        }
    }
}