package de.javaabc.aipopulation.geom;

import java.io.Serializable;

/**
 * A 2D vector of double values.
 *
 * @param x the first entry of this vector
 * @param y the second entry of this vector
 */
public record Vec(double x, double y) implements Serializable {
    /**
     * the zero vector
     */
    public static final Vec ZERO = new Vec(0.0, 0.0);

    /**
     * Creates a new unit vector with given angle.
     *
     * @param radians the angle in radians.
     * @return a new angle with approximate length 1.0 and the given angle
     */
    public static Vec unit(double radians) {
        double x = Math.cos(radians);
        double y = Math.sin(radians);
        return new Vec(x, y);
    }

    /**
     * Creates a new unit vector with given angle.
     *
     * @param angle the angle as a {@link Rot} instance
     * @return a new angle with approximate length 1.0 and the given angle
     */
    public static Vec unit(Rot angle) {
        return unit(angle.radians());
    }

    /**
     * Creates a new vector representing the sum of this and another vector.
     *
     * @param addend the vector to add
     * @return a new vector instance
     */
    public Vec add(Vec addend) {
        return new Vec(x + addend.x, y + addend.y);
    }

    /**
     * Creates a new vector representing the difference of this and another vector.
     *
     * @param subtrahend the vector to subtract
     * @return a new vector instance
     */
    public Vec sub(Vec subtrahend) {
        return new Vec(x - subtrahend.x, y - subtrahend.y);
    }

    /**
     * Creates a new vector representing the sum of this and another vector.
     *
     * @param dx the x-amount to add
     * @param dy the y-amount to add
     * @return a new vector instance
     */
    public Vec add(double dx, double dy) {
        return new Vec(x + dx, y + dy);
    }

    /**
     * Creates a new vector with shifted x-value.
     *
     * @param dx the x-amount to add
     * @return a new vector instance
     */
    public Vec addX(double dx) {
        return new Vec(x + dx, y);
    }

    /**
     * Creates a new vector with shifted x-value.
     *
     * @param dx the x-amount to subtract
     * @return a new vector instance
     */
    public Vec subX(double dx) {
        return new Vec(x - dx, y);
    }

    /**
     * Creates a new vector representing the difference of this and another vector.
     *
     * @param dx the x-amount to subtract
     * @param dy the y-amount to subtract
     * @return a new vector instance
     */
    public Vec sub(double dx, double dy) {
        return new Vec(x - dx, y - dy);
    }

    /**
     * Creates a new vector with scaled entries.
     *
     * @param factor the scaling factor in both x and y direction.
     * @return a new vector instance
     */
    public Vec scale(double factor) {
        return new Vec(factor * x, factor * y);
    }

    /**
     * @return the square of the euclidean length of this vector
     */
    public double squareLength() {
        return x * x + y * y;
    }

    /**
     * @return a {@link Rot} instance representing the rotation of this vector
     */
    public Rot angle() {
        return new Rot(Math.atan2(y, x));
    }

    /**
     * Creates a new vector with restricted bounds.
     *
     * @param minX the minimal x-value of the result vector
     * @param minY the minimal y-value of the result vector
     * @param maxX the maximal x-value of the result vector
     * @param maxY the maximal y-value of the result vector
     * @return a new vector instance with same entries but capped to the given bounds
     */
    public Vec restrict(double minX, double minY, double maxX, double maxY) {
        return new Vec(Math.min(maxX, Math.max(minX, x)), Math.min(maxY, Math.max(minY, y)));
    }

    /**
     * @return true iff at least one of the entries is NaN
     */
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y);
    }
}
