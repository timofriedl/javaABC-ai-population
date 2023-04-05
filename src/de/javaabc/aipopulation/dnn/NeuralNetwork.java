package de.javaabc.aipopulation.dnn;

import java.io.Serializable;
import java.util.Random;

public class NeuralNetwork implements Serializable {
    private final Layer[] layers;

    private NeuralNetwork(Layer[] layers) {
        this.layers = layers;
    }

    public NeuralNetwork(Random random, int... layerSizes) {
        this(new Layer[layerSizes.length - 1]);

        for (int i = 0; i < layers.length - 1; i++)
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], false, random);

        layers[layers.length - 1] = new Layer(layerSizes[layers.length - 1], layerSizes[layers.length], true, random);
    }

    public double[] feedForward(double[] input) {
        for (Layer layer : layers)
            input = layer.forward(input);
        return input;
    }

    public NeuralNetwork mutate(double stddev) {
        Layer[] layersClone = new Layer[layers.length];
        for (int i = 0; i < layers.length; i++)
            layersClone[i] = layers[i].mutate(stddev);
        return new NeuralNetwork(layersClone);
    }

    public static double relu(double x) {
        return Math.max(0.0, x);
    }
}
