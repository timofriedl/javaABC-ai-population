package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Circle;
import de.javaabc.aipopulation.geom.Vec;

import java.awt.*;
import java.io.Serializable;

public class Food extends SimulationObject implements Serializable {
    private final double radius;

    public Food(Vec pos) {
        super(pos, Color.DARK_GRAY);
        this.radius = 5.0;
    }

    @Override
    protected Shape makeBounds() {
        return new Circle(pos, radius);
    }
}
