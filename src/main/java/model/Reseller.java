package model;

/** Data model for a row in the resellers table. */
public record Reseller(int resellerId, int userId, String fullName,
                       String contactNo, String status) {

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
}
