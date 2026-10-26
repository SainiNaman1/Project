package com.railway.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Standardized date and time formatting utilities for the presentation layer.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    public static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIMESTAMP_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private DateUtil() {
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_DISPLAY_FORMAT) : "";
    }

    public static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp != null ? timestamp.format(TIMESTAMP_DISPLAY_FORMAT) : "";
    }

    public static LocalDate parseInputDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
