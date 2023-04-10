package de.javaabc.aipopulation.geom;

import java.awt.geom.Rectangle2D;

/**
 * A geometric rectangle.
 *
 * @author Timo Friedl
 */
public class Rect extends Rectangle2D.Double {
    /**
     * Creates a new rectangle with given position and size.
     *
     * @param pos  the {@link Vec}tor of this rectangle's closest corner to the origin (usually upper left)
     * @param size a {@link Vec}tor representing the diagonal size of this rectangle
     */
    public Rect(Vec pos, Vec size) {
        super(pos.x(), pos.y(), size.x(), size.y());
    }
}
