package model;

/**
 * Data model for a row in the activity_logs table.
 */
public record ActivityLog(int logId, int userId, String action,
                          String description, String logDatetime) {
}
