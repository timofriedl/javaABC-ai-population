package de.javaabc.aipopulation.geom;

import java.io.Serializable;

/**
 * A geometric angle.
 *
 * @param radians the value of this angle measured in radians. 2π = 360°
 * @author Timo Friedl
 */
public record Rot(double radians) implements Serializable {
    /**
     * an angle of 0°
     */
    public static final Rot ZERO = new Rot(0.0);

    /**
     * the constant value of 2π
     */
    private static final double TWO_PI = Math.PI * 2.0;

    /**
     * Creates a new angle from a [0, 1)-normalized value.
     * An input of 1.0 results in an angle of 360°
     *
     * @param value the value of this angle in range [0.0, 1.0)
     * @return a new angle instance
     */
    public static Rot norm(double value) {
        return new Rot(value * TWO_PI);
    }

    /**
     * Creates a new angle with the sum of the values of this and another angle.
     * The result will be normalized to be in range [0, 2π).
     *
     * @param addend the angle to add to this
     * @return a new angle instance
     */
    public Rot add(Rot addend) {
        return new Rot((radians + addend.radians) % TWO_PI);
    }

    /**
     * Creates a new angle with the difference of the values of this and another angle.
     * The result will be normalized to be in range [0, 2π).
     *
     * @param subtrahend the angle to subtract from this
     * @return a new angle instance
     */
    public Rot sub(Rot subtrahend) {
        return new Rot((radians - subtrahend.radians) % TWO_PI);
    }

    /**
     * @return the value of this angle in range [0.0, 1.0)
     */
    public double normalized() {
        return radians % TWO_PI / TWO_PI;
    }

    /**
     * Creates a new angle with a scaled value.
     *
     * @param factor the factor to multiply to this angle
     * @return a new angle instance
     */
    public Rot scale(double factor) {
        return new Rot(radians * factor);
    }
}
