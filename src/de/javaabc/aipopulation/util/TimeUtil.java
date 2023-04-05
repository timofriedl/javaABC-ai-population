package de.javaabc.aipopulation.util;

public class TimeUtil {
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
