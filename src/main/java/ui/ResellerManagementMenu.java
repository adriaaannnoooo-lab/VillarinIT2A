package ui;

import model.Reseller;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Distributor tools for resellers: add, view, search, deactivate, and view consignments. */
public class ResellerManagementMenu {

    private final AppContext ctx;
    private final User admin;
    private final Picker picker;

    public ResellerManagementMenu(AppContext ctx, User admin) {
        this.ctx = ctx;
        this.admin = admin;
        this.picker = new Picker(ctx.input());
    }

    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- MANAGE RESELLERS ---");
            System.out.println("[1] Add reseller profile");
            System.out.println("[2] View all resellers");
            System.out.println("[3] Search resellers");
            System.out.println("[4] Deactivate reseller");
            System.out.println("[5] View reseller consignments, balance & commission");
            System.out.println("[0] Back");
            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> addReseller();
                case "2" -> viewAll();
                case "3" -> new LookupMenu(ctx).search();
                case "4" -> deactivate();
                case "5" -> new LookupMenu(ctx).viewResellerConsignments();
                case "0" -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /** A reseller profile is linked to an existing RESELLER login account. */
    private void addReseller() {
        System.out.println("\n--- ADD RESELLER PROFILE ---");
        List<User> candidates = new ArrayList<>();
        for (User u : ctx.userDao().getAllUsers()) {
            if (User.ROLE_RESELLER.equals(u.role())
                    && ctx.resellerDao().findByUserId(u.userId()).isEmpty()) {
                candidates.add(u);
            }
        }
        if (candidates.isEmpty()) {
            System.out.println("No RESELLER accounts without a profile. Register a reseller account first.");
            return;
        }
        Tables.printUsers(candidates);

        User chosen = null;
        while (chosen == null) {
            int id = ctx.input().readInt("Enter User ID for the new reseller (0 to cancel): ");
            if (id == 0) {
                System.out.println("Add reseller cancelled.");
                return;
            }
            for (User u : candidates) {
                if (u.userId() == id) {
                    chosen = u;
                }
            }
            if (chosen == null) {
                System.out.println("User ID " + id + " is not in the list. Try again.");
            }
        }

        String contact = ctx.input().readLine("Contact number (0 to cancel): ");
        if (contact.equals("0") || contact.isEmpty()) {
            System.out.println("Add reseller cancelled.");
            return;
        }
        if (ctx.resellerDao().addReseller(chosen.userId(), chosen.fullName(), contact)) {
            System.out.println("Success! Reseller profile created for " + chosen.fullName() + ".");
            ctx.logDao().logAction(admin.userId(), "RESELLER_ADDED",
                    "Admin " + admin.userId() + " added reseller User ID " + chosen.userId());
        }
    }

    private void viewAll() {
        System.out.println("\n--- ALL RESELLERS ---");
        List<Reseller> resellers = ctx.resellerDao().getAllResellers();
        if (resellers.isEmpty()) {
            System.out.println("No resellers yet.");
            return;
        }
        Tables.printResellers(resellers);
    }

    /** Soft delete: the reseller record stays, but is marked Inactive. */
    private void deactivate() {
        System.out.println("\n--- DEACTIVATE RESELLER ---");
        List<Reseller> active = new ArrayList<>();
        for (Reseller r : ctx.resellerDao().getAllResellers()) {
            if (Reseller.STATUS_ACTIVE.equals(r.status())) {
                active.add(r);
            }
        }
        if (active.isEmpty()) {
            System.out.println("No active resellers.");
            return;
        }
        Tables.printResellers(active);
        Optional<Reseller> chosen = picker.reseller(active);
        if (chosen.isEmpty()) {
            System.out.println("Deactivate cancelled.");
            return;
        }
        Reseller reseller = chosen.get();
        String confirm = ctx.input().readLine("Type YES to deactivate " + reseller.fullName()
                + " (anything else cancels): ");
        if (!confirm.equals("YES")) {
            System.out.println("Deactivate cancelled.");
            return;
        }
        if (ctx.resellerDao().deactivateReseller(reseller.resellerId())) {
            System.out.println("Success! " + reseller.fullName() + " is now Inactive.");
            ctx.logDao().logAction(admin.userId(), "RESELLER_INACTIVE",
                    "Admin " + admin.userId() + " deactivated Reseller " + reseller.resellerId());
        }
    }
}
