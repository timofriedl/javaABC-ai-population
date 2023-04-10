package de.javaabc.aipopulation.geom;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * A utility class for geometric computations.
 *
 * @author Timo Friedl
 */
public class Geometry {
    /**
     * Computes the union area of given {@link Shape}s.
     *
     * @param shapes the shape objects to union
     * @return an {@link Area} object representing the union of the given shapes
     */
    public static Area unionConvex(Shape... shapes) {
        Path2D path = new Path2D.Double();

        for (Shape shape : shapes)
            path.append(shape, false);

        return new Area(path);
    }
}
