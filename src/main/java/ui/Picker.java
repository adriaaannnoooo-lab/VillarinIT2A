package ui;

import model.ConsignmentView;
import model.Product;
import model.Reseller;

import java.util.List;
import java.util.Optional;

/** Asks for an ID and checks it is in the list shown. Entering 0 cancels. */
final class Picker {

    private final Input input;

    Picker(Input input) {
        this.input = input;
    }

    Optional<Reseller> reseller(List<Reseller> options) {
        while (true) {
            int id = input.readInt("Enter Reseller ID (0 to cancel): ");
            if (id == 0) {
                return Optional.empty();
            }
            for (Reseller r : options) {
                if (r.resellerId() == id) {
                    return Optional.of(r);
                }
            }
            System.out.println("Reseller ID " + id + " is not in the list. Try again.");
        }
    }

    Optional<Product> product(List<Product> options) {
        while (true) {
            int id = input.readInt("Enter Product ID (0 to cancel): ");
            if (id == 0) {
                return Optional.empty();
            }
            for (Product p : options) {
                if (p.productId() == id) {
                    return Optional.of(p);
                }
            }
            System.out.println("Product ID " + id + " is not in the list. Try again.");
        }
    }

    Optional<ConsignmentView> consignment(List<ConsignmentView> options) {
        while (true) {
            int id = input.readInt("Enter Consignment ID (0 to cancel): ");
            if (id == 0) {
                return Optional.empty();
            }
            for (ConsignmentView c : options) {
                if (c.consignmentId() == id) {
                    return Optional.of(c);
                }
            }
            System.out.println("Consignment ID " + id + " is not in the list. Try again.");
        }
    }
}
