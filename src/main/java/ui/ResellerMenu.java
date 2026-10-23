package ui;

import dao.UserDao;
import model.User;

import java.util.Scanner;

/** Menu for the Reseller role (read-only). */
public class ResellerMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final User user;

    public ResellerMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.user = user;
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- RESELLER MENU ---");
            System.out.println("[1] View my consignments");
            System.out.println("[2] View my balance owed");
            System.out.println("[3] View due dates");
            System.out.println("[4] View my commission");
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