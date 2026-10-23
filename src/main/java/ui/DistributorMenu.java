package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

/** Menu for the Distributor (Admin) role. */
public class DistributorMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final User user;

    public DistributorMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.user = user;
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- DISTRIBUTOR MENU ---");
            System.out.println("[1] Manage products");
            System.out.println("[2] Manage resellers");
            System.out.println("[3] Manage staff accounts");
            System.out.println("[4] View reports");
            System.out.println("[5] View activity logs");
            System.out.println("[0] Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1", "2", "3", "4", "5" -> System.out.println("Coming soon.");
                case "0" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}