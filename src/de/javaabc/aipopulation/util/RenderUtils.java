package de.javaabc.aipopulation.util;

import de.javaabc.aipopulation.geom.Vec;

import java.awt.*;

public class RenderUtils {
    public static void drawCenteredString(Graphics2D g, String text, Vec centerPos) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        Vec pos = centerPos.sub(metrics.stringWidth(text) / 2.0, metrics.getHeight() / 2.0 - metrics.getAscent());
        g.drawString(text, (float) pos.x(), (float) pos.y());
    }
}
