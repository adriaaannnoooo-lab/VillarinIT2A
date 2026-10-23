import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Authentication;

import java.util.Scanner;

/** Application entry point: initialize the database, create shared objects, start the UI. */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Konsigna...");

        // 1. Initialize the database
        DatabaseInitializer.initialize();

        // 2. Create the single shared DAO and Scanner
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // 3. Hand control to the authentication screen
        new Authentication(scanner, userDao).start();

        scanner.close();
    }
}