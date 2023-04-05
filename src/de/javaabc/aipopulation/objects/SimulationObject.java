package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.Renderable;

import java.awt.*;
import java.io.Serializable;

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
