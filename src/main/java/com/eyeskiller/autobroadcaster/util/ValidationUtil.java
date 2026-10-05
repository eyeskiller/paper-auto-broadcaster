package com.eyeskiller.autobroadcaster.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern MINI_MESSAGE_PATTERN = Pattern.compile("<[a-zA-Z_][a-zA-Z0-9_#]*[^>]*>");

    private ValidationUtil() {}

    public static boolean isValidTimeFormat(String time) {
        if (time == null || !TIME_PATTERN.matcher(time).matches()) {
            return false;
        }
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isTimeInRange(String timeKey, LocalTime now, int windowMinutes) {
        try {
            LocalTime scheduledTime = LocalTime.parse(timeKey, TIME_FORMATTER);
            long minutesDiff = java.time.Duration.between(scheduledTime, now).toMinutes();
            return minutesDiff >= 0 && minutesDiff < windowMinutes;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static int clampIntervalSeconds(int seconds, int minimum) {
        return Math.max(seconds, minimum);
    }

    public static boolean containsMiniMessageTags(String text) {
        if (text == null) return false;
        return MINI_MESSAGE_PATTERN.matcher(text).find();
    }

    public static boolean containsLegacyColorCodes(String text) {
        return text != null && text.contains("&");
    }
}
