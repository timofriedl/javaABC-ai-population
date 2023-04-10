package de.javaabc.aipopulation.util;

import de.javaabc.aipopulation.geom.Vec;

import java.awt.*;

/**
 * Utility class for everything related to rendering.
 *
 * @author Timo Friedl
 */
public class RenderUtils {
    /**
     * Draws a given text centered on screen.
     *
     * @param g         the {@link Graphics2D} to draw
     * @param text      the text to draw
     * @param centerPos the {@link Vec}tor to the center position to draw the text at
     */
    public static void drawCenteredString(Graphics2D g, String text, Vec centerPos) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        Vec pos = centerPos.sub(metrics.stringWidth(text) / 2.0, metrics.getHeight() / 2.0 - metrics.getAscent());
        g.drawString(text, (float) pos.x(), (float) pos.y());
    }

    /**
     * Decomposes a {@link Color} object to hue-saturation-value space.
     *
     * @param color the color to convert
     * @return a float array containing the values for hue, saturation, value
     */
    public static float[] colorToHsv(Color color) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
        return hsv;
    }

    /**
     * Creates a {@link Color} object from given hue, saturation, value.
     *
     * @param hsv a float array containing the values for hue, saturation, value
     * @return a new {@link Color} object
     */
    public static Color hsvToColor(float[] hsv) {
        return new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
    }
}
