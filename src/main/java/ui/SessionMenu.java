package ui;

import model.User;

import java.util.Scanner;

/** Shown after a successful login. Logging out returns to the main menu. */
public class SessionMenu {

    private final Scanner scanner;
    private final User user;

    public SessionMenu(Scanner scanner, User user) {
        this.scanner = scanner;
        this.user = user;
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- USER MENU ---");
            System.out.println("[1] Logout");
            System.out.print("Choose: ");
            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}