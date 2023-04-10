package de.javaabc.aipopulation.dnn;

import de.javaabc.aipopulation.util.MathUtil;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import static de.javaabc.aipopulation.util.MathUtil.matMul;

/**
 * A layer of a {@link NeuralNetwork}.
 *
 * @author Timo Friedl
 */
public class Layer implements Serializable {
    /**
     * The weight matrix of this layer, including bias.
     * Each row of this matrix corresponds to the weights towards one specific neuron.
     */
    private final double[][] weights;

    /**
     * a flag indicating if the tanh() activation is used instead of ReLU
     */
    private final boolean useTanH;

    /**
     * a pseudorandom number generator used for mutation
     */
    private final Random random;

    /**
     * the maximum absolute value of weights after mutation
     */
    private final double maxWeight;

    /**
     * Creates a new layer of a {@link NeuralNetwork}.
     *
     * @param weights   the weight matrix of this layer, including bias
     * @param useTanH   a flag indicating if the tanh() activation is used instead of ReLU
     * @param random    a pseudorandom number generator used for mutation
     * @param maxWeight the maximum absolute value of weights after mutation
     */
    private Layer(double[][] weights, boolean useTanH, Random random, double maxWeight) {
        this.weights = weights;
        this.useTanH = useTanH;
        this.random = random;
        this.maxWeight = maxWeight;
    }

    /**
     * Creates a new layer of a {@link NeuralNetwork} with xavier glorot initialization.
     *
     * @param inputSize  the number of inputs to this layer
     * @param outputSize the number of outputs to this layer
     * @param useTanH    a flag indicating if the tanh() activation is used instead of ReLU
     * @param random     a pseudorandom number generator used for mutation
     * @param maxWeight  the maximum absolute value of weights after mutation
     */
    public Layer(int inputSize, int outputSize, boolean useTanH, Random random, double maxWeight) {
        this(new double[outputSize][inputSize + 1], useTanH, random, maxWeight);

        for (int y = 0; y < weights.length; y++)
            for (int x = 0; x < weights[0].length; x++)
                weights[y][x] = random.nextGaussian(0.0, Math.sqrt(2.0 / (inputSize + outputSize)));
    }

    /**
     * Computes the output of this layer, given a certain input vector.
     *
     * @param input the input vector
     * @return the computed output vector
     */
    public double[] forward(double[] input) {
        double[] withBias = Arrays.copyOf(input, input.length + 1);
        withBias[input.length] = 1.0;

        // Multiply
        double[] res = matMul(weights, withBias);

        // Activation
        for (int i = 0; i < res.length; i++)
            res[i] = useTanH ? Math.tanh(res[i]) : MathUtil.relu(res[i]);

        return res;
    }

    /**
     * Mutates a certain weight value with zero mean gaussian noise.
     *
     * @param weight the weight value to mutate
     * @param stddev the standard deviation of the noise to add
     * @return the mutated value
     */
    private double mutateWeight(double weight, double stddev) {
        double res = weight + random.nextGaussian(0.0, stddev);

        return Math.min(maxWeight, Math.max(-maxWeight, res));
    }

    /**
     * Mutates this layer with element wise zero mean gaussian noise and returns the result as a new instance.
     *
     * @param stddev the standard deviation of the noise to add
     * @return a new layer with identical weights except added noise
     */
    public Layer mutate(double stddev) {
        double[][] weightsClone = new double[weights.length][weights[0].length];

        for (int y = 0; y < weights.length; y++)
            for (int x = 0; x < weights[0].length; x++)
                weightsClone[y][x] = mutateWeight(weights[y][x], stddev);

        return new Layer(weightsClone, useTanH, random, maxWeight);
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
