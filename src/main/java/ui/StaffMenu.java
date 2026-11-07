package ui;

import model.ConsignmentView;
import model.Product;
import model.Reseller;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Menu for Staff: issue stock, record sold/returned items, and record remittances. */
public class StaffMenu {

    private final AppContext ctx;
    private final User user;
    private final Picker picker;

    public StaffMenu(AppContext ctx, User user) {
        this.ctx = ctx;
        this.user = user;
        this.picker = new Picker(ctx.input());
    }

    public void show() {
        System.out.println("\nWelcome, " + user.fullName() + "!");
        System.out.println("Role: " + user.role() + " | Status: " + user.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- STAFF MENU ---");
            System.out.println("[1] Issue stock to a reseller");
            System.out.println("[2] Record sold / returned items");
            System.out.println("[3] Record a remittance (payment)");
            System.out.println("[4] Search resellers or products");
            System.out.println("[5] View reseller consignments, balance & commission");
            System.out.println("[0] Logout");

            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> issueStock();
                case "2" -> recordSoldReturned();
                case "3" -> recordRemittance();
                case "4" -> new LookupMenu(ctx).search();
                case "5" -> new LookupMenu(ctx).viewResellerConsignments();
                case "0" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void issueStock() {
        System.out.println("\n--- ISSUE STOCK ---");

        List<Reseller> active = new ArrayList<>();
        for (Reseller r : ctx.resellerDao().getAllResellers()) {
            if (Reseller.STATUS_ACTIVE.equals(r.status())) {
                active.add(r);
            }
        }
        if (active.isEmpty()) {
            System.out.println("No active resellers. Ask the distributor to add one.");
            return;
        }
        Tables.printResellers(active);
        Optional<Reseller> reseller = picker.reseller(active);
        if (reseller.isEmpty()) {
            System.out.println("Issue stock cancelled.");
            return;
        }

        List<Product> inStock = new ArrayList<>();
        for (Product p : ctx.productDao().getAllProducts()) {
            if (p.stockQty() > 0) {
                inStock.add(p);
            }
        }
        if (inStock.isEmpty()) {
            System.out.println("No products in stock.");
            return;
        }
        Tables.printProducts(inStock);
        Optional<Product> product = picker.product(inStock);
        if (product.isEmpty()) {
            System.out.println("Issue stock cancelled.");
            return;
        }

        int available = product.get().stockQty();
        int qty = ctx.input().readIntInRange(
                "Quantity to issue (1-" + available + ", 0 to cancel): ", 0, available);
        if (qty == 0) {
            System.out.println("Issue stock cancelled.");
            return;
        }
        int days = ctx.input().readIntInRange("Days until due (1-365, 0 to cancel): ", 0, 365);
        if (days == 0) {
            System.out.println("Issue stock cancelled.");
            return;
        }

        if (ctx.consignmentDao().issueStock(reseller.get().resellerId(),
                product.get().productId(), qty, days)) {
            System.out.println("Success! Issued " + qty + " x " + product.get().productName()
                    + " to " + reseller.get().fullName() + ". Due in " + days + " days.");
            ctx.logDao().logAction(user.userId(), "STOCK_ISSUED",
                    "Staff " + user.userId() + " issued " + qty + " x " + product.get().productName()
                            + " to Reseller " + reseller.get().resellerId());
        } else {
            System.out.println("Could not issue stock. Check the available quantity.");
        }
    }

    private void recordSoldReturned() {
        System.out.println("\n--- RECORD SOLD / RETURNED ITEMS ---");
        ctx.consignmentDao().refreshOverdueStatuses();

        List<ConsignmentView> open = new ArrayList<>();
        for (ConsignmentView c : ctx.consignmentDao().getAll()) {
            if (c.qtyRemaining() > 0) {
                open.add(c);
            }
        }
        if (open.isEmpty()) {
            System.out.println("No consignments with items still out.");
            return;
        }
        Tables.printConsignments(open);
        Optional<ConsignmentView> chosen = picker.consignment(open);
        if (chosen.isEmpty()) {
            System.out.println("Cancelled.");
            return;
        }

        ConsignmentView c = chosen.get();
        int remaining = c.qtyRemaining();
        System.out.println("Items still out for this consignment: " + remaining);
        int sold = ctx.input().readIntInRange(
                "Additional quantity SOLD (0-" + remaining + "): ", 0, remaining);
        int returned = ctx.input().readIntInRange(
                "Additional quantity RETURNED (0-" + (remaining - sold) + "): ", 0, remaining - sold);
        if (sold == 0 && returned == 0) {
            System.out.println("Nothing to update.");
            return;
        }

        if (ctx.consignmentDao().recordSoldReturned(c.consignmentId(), sold, returned)) {
            System.out.println("Success! Consignment " + c.consignmentId() + ": sold +" + sold
                    + ", returned +" + returned + " (returned items went back to stock).");
            ctx.logDao().logAction(user.userId(), "QTY_UPDATED",
                    "Staff " + user.userId() + ": Consignment " + c.consignmentId()
                            + " sold +" + sold + " ret +" + returned);
        } else {
            System.out.println("Could not update the quantities.");
        }
    }

    private void recordRemittance() {
        System.out.println("\n--- RECORD REMITTANCE ---");
        ctx.consignmentDao().refreshOverdueStatuses();

        List<ConsignmentView> owing = new ArrayList<>();
        for (ConsignmentView c : ctx.consignmentDao().getAll()) {
            if (c.balance() > 0) {
                owing.add(c);
            }
        }
        if (owing.isEmpty()) {
            System.out.println("No consignments with a balance owed.");
            return;
        }
        Tables.printConsignments(owing);
        Optional<ConsignmentView> chosen = picker.consignment(owing);
        if (chosen.isEmpty()) {
            System.out.println("Cancelled.");
            return;
        }

        ConsignmentView c = chosen.get();
        double balance = c.balance();
        double amount;
        while (true) {
            amount = ctx.input().readAmount(
                    "Remittance amount (max " + String.format("%.2f", balance) + ", 0 to cancel): ");
            if (amount == 0) {
                System.out.println("Remittance cancelled.");
                return;
            }
            if (amount <= balance + 0.005) {
                break;
            }
            System.out.println("Amount is more than the balance owed. Try again.");
        }

        if (ctx.consignmentDao().recordRemittance(c.consignmentId(), user.userId(), amount)) {
            String newStatus = ctx.consignmentDao().getById(c.consignmentId())
                    .map(ConsignmentView::status).orElse("");
            System.out.println("Success! Recorded " + String.format("%.2f", amount)
                    + " for Consignment " + c.consignmentId() + ". Status: " + newStatus);
            ctx.logDao().logAction(user.userId(), "REMITTANCE_ADDED",
                    "Staff " + user.userId() + " recorded " + String.format("%.2f", amount)
                            + " for Consignment " + c.consignmentId());
        } else {
            System.out.println("Could not record the remittance.");
        }
    }
}
