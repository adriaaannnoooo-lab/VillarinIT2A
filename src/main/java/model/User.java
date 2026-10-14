package model;

/**
 * Data model for a row in the users table.
 * Matches TABLE 6 of the proposal: user_id, username, password, full_name, role, status.
 */
public record User(int userId, String username, String password,
                   String fullName, String role, String status) {

    public static final String ROLE_DISTRIBUTOR = "DISTRIBUTOR";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_RESELLER = "RESELLER";

    public static final String STATUS_PENDING = "PENDING";
}