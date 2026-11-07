import dao.ActivityLogDao;
import dao.ConsignmentDao;
import dao.DatabaseInitializer;
import dao.ProductDao;
import dao.ResellerDao;
import dao.UserDao;
import ui.AppContext;
import ui.Authentication;
import ui.Input;

import java.util.Scanner;

/** Application entry point: initialize the database, create shared objects, start the UI. */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Konsigna...");

        // 1. Initialize the database
        DatabaseInitializer.initialize();

        // 2. Create the single shared Scanner and DAOs
        Scanner scanner = new Scanner(System.in);
        AppContext ctx = new AppContext(scanner, new Input(scanner),
                new UserDao(), new ActivityLogDao(), new ProductDao(),
                new ResellerDao(), new ConsignmentDao());

        // 3. Hand control to the authentication screen
        new Authentication(ctx).start();

        scanner.close();
    }
}
