package de.javaabc.aipopulation.geom;

import java.awt.geom.Rectangle2D;

public class Rect extends Rectangle2D.Double {
    public Rect(Vec pos, Vec size) {
        super(pos.x(), pos.y(), size.x(), size.y());
    }
}
