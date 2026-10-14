import dao.DatabaseConnection;
import ui.MainMenu;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initialize();
        new MainMenu().show();
    }
}