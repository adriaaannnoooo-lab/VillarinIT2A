package ui;

import model.ConsignmentView;
import model.Reseller;

import java.util.ArrayList;
import java.util.List;

/** Reseller Performance & Outstanding Remittance Report (Distributor only). */
public class ReportMenu {

    private final AppContext ctx;

    public ReportMenu(AppContext ctx) {
        this.ctx = ctx;
    }

    public void show() {
        ctx.consignmentDao().refreshOverdueStatuses();
        List<Reseller> resellers = ctx.resellerDao().getAllResellers();
        List<ConsignmentView> views = ctx.consignmentDao().getAll();

        System.out.println("\n=== RESELLER PERFORMANCE & OUTSTANDING REMITTANCE REPORT ===");
        if (resellers.isEmpty()) {
            System.out.println("No resellers yet.");
            return;
        }

        String border = "+" + "-".repeat(7) + "+" + "-".repeat(24) + "+" + "-".repeat(12)
                + "+" + "-".repeat(14) + "+" + "-".repeat(14) + "+";
        String row = "| %-5s | %-22.22s | %-10s | %-12s | %-12s |%n";
        System.out.println(border);
        System.out.printf(row, "ID", "Reseller", "Items Sold", "Commission", "Balance Owed");
        System.out.println(border);

        Reseller top = null;
        int topSold = 0;
        double totalOwed = 0;
        for (Reseller r : resellers) {
            int sold = 0;
            double commission = 0;
            double balance = 0;
            for (ConsignmentView c : views) {
                if (c.resellerId() == r.resellerId()) {
                    sold += c.qtySold();
                    commission += c.commission();
                    balance += c.balance();
                }
            }
            System.out.printf(row, r.resellerId(), r.fullName(), sold,
                    String.format("%.2f", commission), String.format("%.2f", balance));
            totalOwed += balance;
            if (sold > topSold) {
                topSold = sold;
                top = r;
            }
        }
        System.out.println(border);

        System.out.println("Total balance still owed: " + String.format("%.2f", totalOwed));
        if (top == null) {
            System.out.println("Top-selling reseller: none yet (no items sold).");
        } else {
            System.out.println("Top-selling reseller: " + top.fullName() + " (" + topSold + " items sold)");
        }

        List<ConsignmentView> overdue = new ArrayList<>();
        for (ConsignmentView c : views) {
            if (ConsignmentView.OVERDUE.equals(c.status())) {
                overdue.add(c);
            }
        }
        System.out.println("\nOverdue consignments:");
        if (overdue.isEmpty()) {
            System.out.println("None.");
        } else {
            Tables.printConsignments(overdue);
        }
    }
}
