package model;

/**
 * One consignment joined with its reseller, product and total remitted amount.
 *
 * Amount due to the distributor = qtySold x wholesalePrice.
 * Balance owed                  = amount due - total remitted.
 * Reseller commission           = qtySold x (retailPrice - wholesalePrice).
 */
public record ConsignmentView(int consignmentId, int resellerId, String resellerName,
                              String productName, int qtyIssued, int qtySold, int qtyReturned,
                              String dateIssued, String dueDate, String status,
                              double wholesalePrice, double retailPrice, double remitted) {

    public static final String ISSUED = "Issued";
    public static final String PARTIALLY_PAID = "Partially Paid";
    public static final String SETTLED = "Settled";
    public static final String OVERDUE = "Overdue";

    public double amountDue() {
        return qtySold * wholesalePrice;
    }

    public double balance() {
        double b = Math.round((amountDue() - remitted) * 100) / 100.0;
        return Math.max(b, 0);
    }

    public double commission() {
        return qtySold * (retailPrice - wholesalePrice);
    }

    /** Items still out with the reseller (not yet sold or returned). */
    public int qtyRemaining() {
        return qtyIssued - qtySold - qtyReturned;
    }
}
