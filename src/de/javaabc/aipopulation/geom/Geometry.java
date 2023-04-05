package de.javaabc.aipopulation.geom;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class Geometry {
    public static Area unionConvex(Shape... shapes) {
        Path2D path = new Path2D.Double();
        for (Shape shape : shapes) {
            path.append(shape, false);
        }
        return new Area(path);
    }
}
