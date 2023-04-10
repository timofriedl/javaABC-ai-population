package de.javaabc.aipopulation.util;

/**
 * Utility class for math functions.
 *
 * @author Timo Friedl
 */
public class MathUtil {
    /**
     * Performs a matrix-vector multiplication.
     * Each row of the input matrix is multiplied with the vector.
     *
     * @param matrix the matrix to multiply
     * @param vector the vector to multiply
     * @return a new vector that is equal to the result of matrix * vector
     */
    public static double[] matMul(double[][] matrix, double[] vector) {
        double[] result = new double[matrix.length];

        for (int y = 0; y < result.length; y++)
            for (int x = 0; x < vector.length; x++)
                result[y] += matrix[y][x] * vector[x];

        return result;
    }

    /**
     * The rectified linear unit function.
     *
     * @param x some input value
     * @return x, if x > 0, 0 otherwise
     */
    public static double relu(double x) {
        return Math.max(0.0, x);
    }
}
