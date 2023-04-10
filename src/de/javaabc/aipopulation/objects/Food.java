package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Circle;
import de.javaabc.aipopulation.geom.Vec;

import java.awt.*;
import java.io.Serializable;

/**
 * A small dot that gives {@link Individual}s energy when eaten.
 *
 * @author Timo Friedl
 */
public class Food extends SimulationObject implements Serializable {
    /**
     * the radius of the food dot
     */
    private final double radius;

    /**
     * Creates a new food object at a given position on screen.
     *
     * @param pos the {@link Vec}tor to the center point of this food object
     */
    public Food(Vec pos) {
        super(pos, Color.DARK_GRAY);
        this.radius = 5.0; // Default radius is always 5.0
    }

    @Override
    protected Shape makeBounds() {
        return new Circle(pos, radius);
    }
}
