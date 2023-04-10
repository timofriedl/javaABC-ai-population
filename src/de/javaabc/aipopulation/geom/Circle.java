package de.javaabc.aipopulation.geom;

import java.awt.geom.Ellipse2D;

/**
 * A geometric circle.
 *
 * @author Timo Friedl
 */
public class Circle extends Ellipse2D.Double {
    /**
     * Creates a new circle given its center position and radius.
     *
     * @param pos    the center position {@link Vec}tor of this circle
     * @param radius the radius of this circle
     */
    public Circle(Vec pos, double radius) {
        super(pos.x() - radius, pos.y() - radius, 2.0 * radius, 2.0 * radius);
    }
}
