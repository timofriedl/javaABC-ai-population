package de.javaabc.aipopulation.geom;

import java.awt.geom.Line2D;

public class Line extends Line2D.Double {
    public Line(Vec from, Vec to) {
        super(from.x(), from.y(), to.x(), to.y());
    }
}
