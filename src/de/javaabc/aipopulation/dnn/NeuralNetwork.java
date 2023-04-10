package de.javaabc.aipopulation.dnn;

import java.io.Serializable;
import java.util.Random;

/**
 * A basic neural network consisting of ReLU-activated {@link Layer}s and a tanh-activated output {@link Layer}.
 *
 * @author Timo Friedl
 */
public class NeuralNetwork implements Serializable {
    /**
     * the layers in this neural network
     */
    private final Layer[] layers;

    /**
     * a pseudorandom number generator for weight mutations
     */
    private final Random random;

    /**
     * Creates a new neural network given its layers.
     *
     * @param layers the layers in this neural network
     */
    private NeuralNetwork(Layer[] layers) {
        this.layers = layers;
        random = new Random();
    }

    /**
     * Creates a new neural network with (pseudo-)randomly initialized layers of given sizes.
     *
     * @param layerSizes the number of neurons in each layer, ordered input - hidden - output
     * @param maxWeight  the maximum absolute value of layer weights after mutation
     */
    public NeuralNetwork(double maxWeight, int... layerSizes) {
        this(new Layer[layerSizes.length - 1]);

        for (int i = 0; i < layers.length - 1; i++)
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], false, random, maxWeight);

        layers[layers.length - 1] = new Layer(layerSizes[layers.length - 1], layerSizes[layers.length], true, random, maxWeight);
    }

    /**
     * Computes the output of this {@link NeuralNetwork}, given a certain input vector.
     *
     * @param input the input vector
     * @return the computed output vector
     */
    public double[] feedForward(double[] input) {
        for (Layer layer : layers)
            input = layer.forward(input);

        return input;
    }

    /**
     * Mutates all layers of this {@link NeuralNetwork} with zero mean gaussian noise.
     *
     * @param stddev the standard deviation for the added noise of each weight value
     * @return a new neural network with mutated weights
     */
    public NeuralNetwork mutate(double stddev) {
        Layer[] layersClone = new Layer[layers.length];

        for (int i = 0; i < layers.length; i++)
            layersClone[i] = layers[i].mutate(stddev);

        return new NeuralNetwork(layersClone);
    }
}
