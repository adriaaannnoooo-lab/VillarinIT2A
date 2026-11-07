package ui;

import model.ConsignmentView;
import model.Reseller;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Menu for the Reseller role. Read-only: a reseller only sees their own records. */
public class ResellerMenu {

    private final AppContext ctx;
    private final User user;

    public ResellerMenu(AppContext ctx, User user) {
        this.ctx = ctx;
        this.user = user;
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        Optional<Reseller> profile = ctx.resellerDao().findByUserId(user.userId());
        if (profile.isEmpty()) {
            System.out.println("Your reseller profile has not been set up yet. Please contact the distributor.");
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- RESELLER MENU ---");
            System.out.println("[1] View my consignments");
            System.out.println("[2] View my balance owed");
            System.out.println("[3] View my due dates");
            System.out.println("[4] View my commission");
            System.out.println("[0] Logout");

            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> showConsignments(profile, "MY CONSIGNMENTS", false);
                case "2" -> showBalance(profile);
                case "3" -> showConsignments(profile, "MY DUE DATES (unsettled consignments)", true);
                case "4" -> showCommission(profile);
                case "0" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private List<ConsignmentView> myConsignments(Optional<Reseller> profile) {
        if (profile.isEmpty()) {
            return new ArrayList<>();
        }
        ctx.consignmentDao().refreshOverdueStatuses();
        return ctx.consignmentDao().getByReseller(profile.get().resellerId());
    }

    private void showConsignments(Optional<Reseller> profile, String title, boolean unsettledOnly) {
        System.out.println("\n--- " + title + " ---");
        List<ConsignmentView> list = new ArrayList<>();
        for (ConsignmentView c : myConsignments(profile)) {
            if (!unsettledOnly || !ConsignmentView.SETTLED.equals(c.status())) {
                list.add(c);
            }
        }
        if (list.isEmpty()) {
            System.out.println("Nothing to show.");
            return;
        }
        Tables.printConsignments(list);
    }

    private void showBalance(Optional<Reseller> profile) {
        System.out.println("\n--- MY BALANCE OWED ---");
        List<ConsignmentView> list = myConsignments(profile);
        double total = 0;
        for (ConsignmentView c : list) {
            total += c.balance();
        }
        System.out.println("Total balance owed: " + String.format("%.2f", total));
        if (!list.isEmpty()) {
            Tables.printConsignments(list);
        }
    }

    private void showCommission(Optional<Reseller> profile) {
        System.out.println("\n--- MY COMMISSION ---");
        List<ConsignmentView> list = myConsignments(profile);
        if (list.isEmpty()) {
            System.out.println("Nothing to show.");
            return;
        }
        Tables.printConsignments(list);
        Tables.printTotals(list);
    }
}
