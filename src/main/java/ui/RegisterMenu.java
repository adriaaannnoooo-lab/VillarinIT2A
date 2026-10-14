package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

/** Register screen: collects details and saves a PENDING account. */
public class RegisterMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public RegisterMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void show() {
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
}