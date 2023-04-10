package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.Renderable;
import de.javaabc.aipopulation.world.ThreadSafeContainer;

import java.awt.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public abstract class SimulationObject implements Renderable, Serializable {
    protected Vec pos;
    protected Color color;
    protected transient Shape bounds;

    public SimulationObject(Vec pos, Color color) {
        this.pos = pos;
        this.color = color;
    }

    protected abstract Shape makeBounds();

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fill(makeBounds());
    }

    /**
     * Creates a vector representing the difference of positions of another object and this.
     *
     * @param other the target object
     * @return a new vector instance
     */
    public Vec vectorTo(SimulationObject other) {
        return other.pos.sub(pos);
    }

    /**
     * Searches for the closest {@link SimulationObject} of a given {@link ThreadSafeContainer}.
     *
     * @param objects a collection of {@link SimulationObject}s to search through
     * @param <T>     the type of objects to search through
     * @return an optional containing the closest of the given objects, or an empty optional if there is no other object
     */
    public <T extends SimulationObject> Optional<T> findClosest(ThreadSafeContainer<T> objects) {
        return objects.stream(false)
                .filter(obj -> obj != this)
                .map(obj -> Map.entry(obj, obj.getPos().sub(pos).squareLength()))
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    public Shape getBounds() {
        return bounds == null ? makeBounds() : bounds;
    }

    public Vec getPos() {
        return pos;
    }

    public Color getColor() {
        return color;

    }
}
