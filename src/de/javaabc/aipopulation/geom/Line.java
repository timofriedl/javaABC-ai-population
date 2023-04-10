package de.javaabc.aipopulation.geom;

import java.awt.geom.Line2D;

/**
 * A geometric line segment.
 *
 * @author Timo Friedl
 */
public class Line extends Line2D.Double {
    /**
     * Creates a new line from a given point to another.
     *
     * @param from the {@link Vec}tor of the starting point
     * @param to   the {@link Vec}tor of the end point
     */
    public Line(Vec from, Vec to) {
        super(from.x(), from.y(), to.x(), to.y());
    }
}
