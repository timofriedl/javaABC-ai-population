package de.javaabc.aipopulation.geom;

import java.io.Serializable;

public record Vec(double x, double y) implements Serializable {
    public static final Vec ZERO = new Vec(0.0, 0.0);

    public static Vec unit(double radians) {
        double x = Math.cos(radians);
        double y = Math.sin(radians);
        return new Vec(x, y);
    }

    public static Vec unit(Rot angle) {
        return unit(angle.radians());
    }

    public Vec add(Vec addend) {
        return new Vec(x + addend.x, y + addend.y);
    }

    public Vec sub(Vec subtrahend) {
        return new Vec(x - subtrahend.x, y - subtrahend.y);
    }

    public Vec add(double dx, double dy) {
        return new Vec(x + dx, y + dy);
    }

    public Vec addX(double dx) {
        return new Vec(x + dx, y);
    }

    public Vec subX(double dx) {
        return new Vec(x - dx, y);
    }

    public Vec sub(double dx, double dy) {
        return new Vec(x - dx, y - dy);
    }

    public Vec scale(double factor) {
        return new Vec(factor * x, factor * y);
    }

    public double squareLength() {
        return x * x + y * y;
    }

    public Rot angle() {
        return new Rot(Math.atan2(y, x));
    }

    public Vec restrict(double minX, double minY, double maxX, double maxY) {
        return new Vec(Math.min(maxX, Math.max(minX, x)), Math.min(maxY, Math.max(minY, y)));
    }
}
