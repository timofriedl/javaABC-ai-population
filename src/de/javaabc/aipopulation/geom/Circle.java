package de.javaabc.aipopulation.geom;

import java.awt.geom.Ellipse2D;

public class Circle extends Ellipse2D.Double {
    public Circle(Vec pos, double radius) {
        super(pos.x() - radius, pos.y() - radius, 2.0 * radius, 2.0 * radius);
    }
}
