package ui;

import model.ConsignmentView;
import model.Product;
import model.Reseller;
import model.User;

import java.util.List;

/** Aligned console tables built with printf formatters. */
final class Tables {

    private Tables() { }

    private static String border(int... widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    static void printUsers(List<User> users) {
        String border = border(5, 15, 12, 10);
        String row = "| %-5s | %-15.15s | %-12s | %-10s |%n";
        System.out.println(border);
        System.out.printf(row, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(row, u.userId(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }

    static void printProducts(List<Product> products) {
        String border = border(5, 22, 10, 10, 6);
        String row = "| %-5s | %-22.22s | %-10s | %-10s | %-6s |%n";
        System.out.println(border);
        System.out.printf(row, "ID", "Product Name", "Wholesale", "Retail", "Stock");
        System.out.println(border);
        for (Product p : products) {
            System.out.printf(row, p.productId(), p.productName(),
                    String.format("%.2f", p.wholesalePrice()),
                    String.format("%.2f", p.retailPrice()), p.stockQty());
        }
        System.out.println(border);
        System.out.println("Total: " + products.size());
    }

    static void printResellers(List<Reseller> resellers) {
        String border = border(5, 8, 22, 14, 10);
        String row = "| %-5s | %-8s | %-22.22s | %-14.14s | %-10s |%n";
        System.out.println(border);
        System.out.printf(row, "ID", "User ID", "Full Name", "Contact", "Status");
        System.out.println(border);
        for (Reseller r : resellers) {
            System.out.printf(row, r.resellerId(), r.userId(), r.fullName(), r.contactNo(), r.status());
        }
        System.out.println(border);
        System.out.println("Total: " + resellers.size());
    }

    static void printConsignments(List<ConsignmentView> list) {
        String border = border(5, 14, 16, 6, 5, 5, 10, 14, 10, 10);
        String row = "| %-5s | %-14.14s | %-16.16s | %-6s | %-5s | %-5s | %-10s | %-14s | %-10s | %-10s |%n";
        System.out.println(border);
        System.out.printf(row, "ID", "Reseller", "Product", "Issued", "Sold", "Ret.",
                "Due Date", "Status", "Balance", "Commission");
        System.out.println(border);
        for (ConsignmentView c : list) {
            System.out.printf(row, c.consignmentId(), c.resellerName(), c.productName(),
                    c.qtyIssued(), c.qtySold(), c.qtyReturned(), c.dueDate(), c.status(),
                    String.format("%.2f", c.balance()), String.format("%.2f", c.commission()));
        }
        System.out.println(border);
        System.out.println("Total: " + list.size());
    }

    static void printTotals(List<ConsignmentView> list) {
        int sold = 0;
        double balance = 0;
        double commission = 0;
        for (ConsignmentView c : list) {
            sold += c.qtySold();
            balance += c.balance();
            commission += c.commission();
        }
        System.out.println("Items sold: " + sold
                + " | Balance owed: " + String.format("%.2f", balance)
                + " | Commission: " + String.format("%.2f", commission));
    }
}
