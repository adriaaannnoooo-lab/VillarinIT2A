package ui;

import model.ConsignmentView;
import model.Product;
import model.Reseller;

import java.util.List;
import java.util.Optional;

/** Read-only lookups shared by the Distributor and Staff: search, and a reseller's consignments. */
public class LookupMenu {

    private final AppContext ctx;
    private final Picker picker;

    public LookupMenu(AppContext ctx) {
        this.ctx = ctx;
        this.picker = new Picker(ctx.input());
    }

    /** Search resellers or products by ID or name. */
    public void search() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- SEARCH ---");
            System.out.println("[1] Search resellers (ID or name)");
            System.out.println("[2] Search products (ID or name)");
            System.out.println("[0] Back");
            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> searchResellers();
                case "2" -> searchProducts();
                case "0" -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void searchResellers() {
        String keyword = ctx.input().readLine("Enter reseller ID or name (0 to cancel): ");
        if (keyword.equals("0") || keyword.isEmpty()) {
            System.out.println("Search cancelled.");
            return;
        }
        List<Reseller> found = ctx.resellerDao().searchResellers(keyword);
        if (found.isEmpty()) {
            System.out.println("No resellers found.");
        } else {
            Tables.printResellers(found);
        }
    }

    private void searchProducts() {
        String keyword = ctx.input().readLine("Enter product ID or name (0 to cancel): ");
        if (keyword.equals("0") || keyword.isEmpty()) {
            System.out.println("Search cancelled.");
            return;
        }
        List<Product> found = ctx.productDao().searchProducts(keyword);
        if (found.isEmpty()) {
            System.out.println("No products found.");
        } else {
            Tables.printProducts(found);
        }
    }

    /** Shows one reseller's consignments, balance owed and commission. */
    public void viewResellerConsignments() {
        System.out.println("\n--- RESELLER CONSIGNMENTS, BALANCE & COMMISSION ---");
        ctx.consignmentDao().refreshOverdueStatuses();
        List<Reseller> resellers = ctx.resellerDao().getAllResellers();
        if (resellers.isEmpty()) {
            System.out.println("No resellers found.");
            return;
        }
        Tables.printResellers(resellers);
        Optional<Reseller> chosen = picker.reseller(resellers);
        if (chosen.isEmpty()) {
            System.out.println("Cancelled.");
            return;
        }
        List<ConsignmentView> views = ctx.consignmentDao().getByReseller(chosen.get().resellerId());
        if (views.isEmpty()) {
            System.out.println("This reseller has no consignments yet.");
            return;
        }
        Tables.printConsignments(views);
        Tables.printTotals(views);
    }
}
