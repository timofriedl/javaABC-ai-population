package de.javaabc.aipopulation.geom;

import java.io.Serializable;

public record Rot(double radians) implements Serializable {
    public static final Rot ZERO = new Rot(0.0);

    private static final double TWO_PI = Math.PI * 2.0;

    public static Rot norm(double value) {
        return new Rot(value * TWO_PI);
    }

    public Rot add(Rot addend) {
        return new Rot((radians + addend.radians) % TWO_PI);
    }

    public Rot sub(Rot subtrahend) {
        return new Rot((radians - subtrahend.radians) % TWO_PI);
    }

    public double normalized() {
        return radians % TWO_PI / TWO_PI;
    }

    public Rot scale(double factor) {
        return new Rot(radians * factor);
    }
}
