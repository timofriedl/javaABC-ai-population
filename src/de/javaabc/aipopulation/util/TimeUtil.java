package de.javaabc.aipopulation.util;

/**
 * Utility class for everything time related.
 *
 * @author Timo Friedl
 */
public class TimeUtil {
    /**
     * Formats a given number of ticks to a duration string.
     *
     * @param ticks the number of ticks
     * @return the created duration string
     */
    public static String formatDuration(long ticks) {
        double hoursExact = ticks / (60.0 * 60.0 * 60.0);
        int hours = (int) hoursExact;
        int minutes = (int) ((hoursExact - hours) * 60.0);
        int seconds = (int) (((hoursExact - hours) * 60.0 - minutes) * 60.0);

        if (hours == 0)
            return String.format("%d:%02d", minutes, seconds);
        else
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
