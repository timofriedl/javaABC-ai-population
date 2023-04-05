package de.javaabc.aipopulation.util;

public class MathUtil {
    public static double[] matMul(double[][] matrix, double[] vector) {
        double[] result = new double[matrix.length];

        for (int y = 0; y < result.length; y++)
            for (int x = 0; x < vector.length; x++)
                result[y] += matrix[y][x] * vector[x];

        return result;
    }
}
