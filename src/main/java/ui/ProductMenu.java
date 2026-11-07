package ui;

import model.Product;
import model.User;

import java.util.List;
import java.util.Optional;

/** Distributor tools for products: add, view, search, and remove discontinued products. */
public class ProductMenu {

    private final AppContext ctx;
    private final User admin;
    private final Picker picker;

    public ProductMenu(AppContext ctx, User admin) {
        this.ctx = ctx;
        this.admin = admin;
        this.picker = new Picker(ctx.input());
    }

    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- MANAGE PRODUCTS ---");
            System.out.println("[1] Add product");
            System.out.println("[2] View all products");
            System.out.println("[3] Search products");
            System.out.println("[4] Remove discontinued product");
            System.out.println("[0] Back");
            switch (ctx.input().readLine("Choose: ")) {
                case "1" -> addProduct();
                case "2" -> viewAll();
                case "3" -> new LookupMenu(ctx).search();
                case "4" -> removeProduct();
                case "0" -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void addProduct() {
        System.out.println("\n--- ADD PRODUCT ---");
        String name = ctx.input().readLine("Product name (0 to cancel): ");
        if (name.equals("0")) {
            System.out.println("Add product cancelled.");
            return;
        }
        if (name.isEmpty()) {
            System.out.println("Product name cannot be empty.");
            return;
        }
        double wholesale = ctx.input().readAmount("Wholesale price: ");
        double retail = ctx.input().readAmount("Retail price: ");
        if (retail < wholesale) {
            System.out.println("Retail price cannot be lower than the wholesale price.");
            return;
        }
        int stock = ctx.input().readIntInRange("Stock quantity: ", 0, 1_000_000);

        if (ctx.productDao().addProduct(name, wholesale, retail, stock)) {
            System.out.println("Success! Product \"" + name + "\" added.");
            ctx.logDao().logAction(admin.userId(), "PRODUCT_ADDED",
                    "Admin " + admin.userId() + " added product: " + name);
        }
    }

    private void viewAll() {
        System.out.println("\n--- ALL PRODUCTS ---");
        List<Product> products = ctx.productDao().getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products yet.");
            return;
        }
        Tables.printProducts(products);
    }

    private void removeProduct() {
        System.out.println("\n--- REMOVE DISCONTINUED PRODUCT ---");
        List<Product> products = ctx.productDao().getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products to remove.");
            return;
        }
        Tables.printProducts(products);
        Optional<Product> chosen = picker.product(products);
        if (chosen.isEmpty()) {
            System.out.println("Remove cancelled.");
            return;
        }
        Product product = chosen.get();

        if (ctx.productDao().isProductInUse(product.productId())) {
            System.out.println("Cannot remove \"" + product.productName()
                    + "\": it is used in existing consignments.");
            return;
        }
        String confirm = ctx.input().readLine("Type YES to remove \"" + product.productName()
                + "\" (anything else cancels): ");
        if (!confirm.equals("YES")) {
            System.out.println("Remove cancelled.");
            return;
        }
        if (ctx.productDao().deleteProduct(product.productId())) {
            System.out.println("Success! Product removed.");
            ctx.logDao().logAction(admin.userId(), "PRODUCT_DELETED",
                    "Admin " + admin.userId() + " removed product: " + product.productName());
        }
    }
}
