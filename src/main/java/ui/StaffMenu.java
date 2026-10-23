package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

/** Menu for the Staff role. */
public class StaffMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final User user;

    public StaffMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.user = user;
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- STAFF MENU ---");
            System.out.println("[1] Issue stock to a reseller");
            System.out.println("[2] Record sold / returned items");
            System.out.println("[3] Record a remittance");
            System.out.println("[4] Search resellers or products");
            System.out.println("[0] Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1", "2", "3", "4" -> System.out.println("Coming soon.");
                case "0" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}