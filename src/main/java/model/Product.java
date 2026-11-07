package model;

/** Data model for a row in the products table. */
public record Product(int productId, String productName, double wholesalePrice,
                      double retailPrice, int stockQty) {
}
