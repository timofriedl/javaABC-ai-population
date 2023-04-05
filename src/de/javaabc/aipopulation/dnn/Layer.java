package de.javaabc.aipopulation.dnn;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import static de.javaabc.aipopulation.util.MathUtil.matMul;

public class Layer implements Serializable {
    private final double[][] weights;
    private final boolean useTanH;
    private final Random random;

    public Layer(double[][] weights, boolean useTanH, Random random) {
        this.weights = weights;
        this.useTanH = useTanH;
        this.random = random;
    }

    public Layer(int inputSize, int outputSize, boolean useTanH, Random random) {
        this(new double[outputSize][inputSize + 1], useTanH, random);

        for (int y = 0; y < weights.length; y++)
            for (int x = 0; x < weights[0].length; x++)
                weights[y][x] = random.nextGaussian(0.0, Math.sqrt(2.0 / (inputSize + outputSize)));
    }

    public double[] forward(double[] input) {
        double[] withBias = Arrays.copyOf(input, input.length + 1);
        withBias[input.length] = 1.0;

        double[] res = matMul(weights, withBias);
        for (int i = 0; i < res.length; i++)
            res[i] = useTanH ? Math.tanh(res[i]) : NeuralNetwork.relu(res[i]);
        return res;
    }

    private double mutateWeight(double weight, double stddev) {
        double res = weight + random.nextGaussian(0.0, stddev);
        if (res > 10.0)
            res = 10.0;
        else if (res < -10.0)
            res = -10.0;
        return res;
    }

    public Layer mutate(double stddev) {
        double[][] weightsClone = new double[weights.length][weights[0].length];
        for (int y = 0; y < weights.length; y++)
            for (int x = 0; x < weights[0].length; x++)
                weightsClone[y][x] = mutateWeight(weights[y][x], stddev);
        return new Layer(weightsClone, useTanH, random);
    }

    @Override
    public String toString() {
        var df = new DecimalFormat(" #,##0.00;-#");
        return Arrays.stream(weights)
                .map(row -> Arrays.stream(row)
                        .mapToObj(df::format)
                        .collect(Collectors.joining(" ", "[", "]")))
                .collect(Collectors.joining("\n ", "[", "]"));
    }
}
