package ui;

import dao.UserDao;

import java.util.Scanner;

/** The first screen: Register, Login, or Exit. */
public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final RegisterMenu registerMenu = new RegisterMenu(scanner, userDao);
    private final LoginMenu loginMenu = new LoginMenu(scanner, userDao);

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=======================================");
            System.out.println(" KONSIGNA");
            System.out.println("=======================================");
            System.out.println("[1] Register");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> registerMenu.show();
                case "2" -> loginMenu.show();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Enter 1, 2, or 3.");
            }
        }
        scanner.close();
    }
}