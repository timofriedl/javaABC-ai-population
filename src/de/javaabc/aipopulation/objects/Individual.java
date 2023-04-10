package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.Simulation;
import de.javaabc.aipopulation.dnn.NeuralNetwork;
import de.javaabc.aipopulation.geom.Circle;
import de.javaabc.aipopulation.geom.Rect;
import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.RenderUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static de.javaabc.aipopulation.geom.Geometry.unionConvex;

/**
 * A small bacteria-like individual that can move and rotate, eat {@link Food} or other individuals,
 * and reproduce itself.
 * <p>
 * The brain of each individual is a {@link NeuralNetwork} that computes the actions for the next tick in the {@link Simulation}.
 *
 * @author Timo Friedl
 */
public class Individual extends MovableObject implements Comparable<Individual>, Serializable {
    /**
     * the maximum absolute translational acceleration, measured in px / tick^2
     */
    private static final double MAX_ACC = 0.3;

    /**
     * the maximum absolute rotational acceleration, measured in radians / tick^2
     */
    private static final double MAX_ROT_ACC = 0.02;

    /**
     * the portion of translational speed that is retained in each tick (must be <= 1.0)
     */
    private static final double TRANSLATIONAL_FRICTION = 0.9;

    /**
     * the portion of rotational speed that is retained in each tick (must be <= 1.0)
     */
    private static final double ROTATIONAL_FRICTION = 0.7;

    /**
     * the portion of speed that is retained when an individual bounces against the simulation boundary
     */
    private static final double COLLISION_DAMPING = 0.5;

    /**
     * the amount of "energy" that is deducted at each tick for (squared) translational speed
     */
    private static final double MOVING_COST = 0.001;

    /**
     * the amount of "energy" that is deducted at each tick for (squared) rotational speed
     */
    private static final double ROTATION_COST = 1000.0;

    /**
     * the amount of "energy" that is deducted at each tick even if the individual is not moving at all
     */
    private static final double GENERAL_COST = 0.002;

    /**
     * the amount of "energy" an individual can steal in one tick from another individual by eating it
     */
    private static final double EAT_RATE = 0.005;

    /**
     * the portion of "energy" an individual receives of the stolen energy when eating another individual
     */
    private static final double EAT_EFFICIENCY = 0.5;

    /**
     * the maximum "energy" an individual could potentially have
     */
    private static final double MAX_ENERGY = 200.0;

    /**
     * the minimum mutation factor
     */
    private static final double MIN_MUTATION_FACTOR = 1E-4;

    /**
     * the reference to the main {@link Simulation}
     */
    private transient Simulation simulation;

    /**
     * the radius of the front and back {@link Circle} of this individual, measured in px
     */
    private final double radius;

    /**
     * the distance between the center position of the front (or back) circle to this individual's position
     * <p>
     * 2.0 * halfTorsoLength = distance between front circle to back circle
     */
    private final double halfTorsoLength;

    /**
     * The energy points of this individual.
     * This is not the physical energy, but rather a measure of fitness.
     * The more energy, the better chances of survival.
     */
    private double energy;

    /**
     * the {@link NeuralNetwork} that computes the actions of this individual
     */
    private final NeuralNetwork brain;

    /**
     * the visual system of this individual
     */
    private final Eye eye;

    /**
     * a pseudorandom number generator for mutation
     */
    private final Random random;

    /**
     * the standard deviation of the zero mean gaussian noise that is added when being mutated
     */
    private final double mutationFactor;

    /**
     * the age of this individual, measured in ticks
     */
    private long age;

    /**
     * the generation of this individual
     */
    private final long generation;

    /**
     * An array of double values for this individual's memory.
     * There are way better concepts for state-preserving neural networks (e.g. LSTMs),
     * this is only a trivial first approach.
     */
    private double[] memory;

    /**
     * a flag indicating whether this individual wants to eat a possibly colliding individual or not
     */
    private boolean wantToEat;

    /**
     * a flag indicating whether this individual is currently eating a colliding individual
     */
    private boolean eating;

    /**
     * a flag indicating whether this individual is currently being eaten by another
     */
    private boolean beingEaten;

    /**
     * a flag indicating whether this individual even wants to reproduce itself
     */
    private boolean wantToReproduce;

    /**
     * Creates a new individual.
     *
     * @param simulation      the reference to the main {@link Simulation}
     * @param pos             the center position of this individual, measured in px
     * @param speed           the {@link Vec}tor of translational speed of this individual, measured in px / tick
     * @param rot             the {@link Rot}ation of this individual
     * @param rotSpeed        the rotation speed of this individual, measured in radians / tick
     * @param color           the render color of this individual
     * @param halfTorsoLength the distance between the center position of the front (or back) circle to this individual's position
     * @param energy          the fitness of this individual
     * @param radius          the radius of the front and back {@link Circle} of this individual, measured in px
     * @param brain           the {@link NeuralNetwork} that computes the actions of this individual
     * @param mutationFactor  the standard deviation of the zero mean gaussian noise that is added when being mutated
     * @param memory          an array of double values for this individual's memory
     * @param generation      the generation of this individual
     */
    private Individual(Simulation simulation, Vec pos, Vec speed, Rot rot, Rot rotSpeed, Color color, double halfTorsoLength, double energy,
                       double radius, NeuralNetwork brain, double mutationFactor, double[] memory, long generation) {
        super(pos, speed, rot, rotSpeed, color);
        this.simulation = simulation;
        this.halfTorsoLength = halfTorsoLength;
        this.energy = energy;
        this.radius = radius;
        this.brain = brain;
        this.mutationFactor = mutationFactor;
        this.memory = Arrays.copyOf(memory, memory.length);
        this.generation = generation;

        eye = new Eye(simulation, this);
        random = new Random();
    }

    /**
     * Creates a new individual with default values.
     *
     * @param simulation the reference to the main {@link Simulation}
     * @param pos        the center position of this individual, measured in px
     * @param rot        the {@link Rot}ation of this individual
     * @param color      the render color of this individual
     * @param radius     the radius of the front and back {@link Circle} of this individual, measured in px
     */
    public Individual(Simulation simulation, Vec pos, Rot rot, Color color, double radius) {
        this(simulation, pos, Vec.ZERO, rot, Rot.ZERO, color, 10.0, 100.0, radius,
                new NeuralNetwork(10.0, 12 + 5, 12, 12, 4 + 5),
                1E-2, new double[5], 0L
        );
    }

    /**
     * Collects the inputs for the neural network and computes its outputs.
     */
    private void tickBrain() {
        double[] inputs = {
                energy / 100.0, // The current fitness
                pos.x() / simulation.getWidth(), // The horizontal position on screen
                pos.y() / simulation.getHeight(), // The vertical position on screen
                speed.x(), // The horizontal speed
                speed.y(), // The vertical speed
                rotSpeed.normalized(), // The rotational speed
                eye.getDetectedFoodRotation().normalized(), // The direction to the closest food object
                10_000.0 / eye.getDetectedFoodSqDistance(), // The distance to the closest food object
                eye.getDetectedEnemyRotation().normalized(), // The direction to the closest other individual
                10_000.0 / eye.getDetectedEnemySqDistance(), // The distance to the closest other individual
                eye.getDetectedEnemyHue(), // The hue of the color of the closest other individual
                eye.getDetectedEnemySaturation(), // The saturation of the color of the closest other individual
                memory[0], // Memory slot 0
                memory[1], // Memory slot 1
                memory[2], // Memory slot 2
                memory[3], // Memory slot 3
                memory[4] // Memory slot 4
        };

        // Compute neural network output
        double[] output = brain.feedForward(inputs);

        // Save memory output
        memory = Arrays.copyOfRange(output, output.length - memory.length, output.length);

        // Save acceleration
        acc = Vec.unit(rot).scale(output[0] * MAX_ACC);
        rotAcc = new Rot(output[1] * MAX_ROT_ACC);

        // Save preferences
        wantToEat = output[2] > 0.0;
        wantToReproduce = output[3] > 0.0;
    }

    /**
     * Handles collisions with the simulation boundary.
     */
    private void tickWallCollisions() {
        int w = simulation.getWidth(), h = simulation.getHeight();

        if (speed.x() < 0 && getBounds().intersects(-100, 0, 100, h)) {
            pos = new Vec(getBounds().getBounds2D().getWidth() / 2.0, pos.y());
            speed = new Vec(-speed.x(), speed.y()).scale(COLLISION_DAMPING);
        } else if (speed.x() > 0 && getBounds().intersects(w, 0, 100, h)) {
            pos = new Vec(w - getBounds().getBounds2D().getWidth() / 2.0, pos.y());
            speed = new Vec(-speed.x(), speed.y()).scale(COLLISION_DAMPING);
        }

        if (speed.y() < 0 && getBounds().intersects(0, -100, w, 100)) {
            pos = new Vec(pos.x(), getBounds().getBounds2D().getHeight() / 2.0);
            speed = new Vec(speed.x(), -speed.y()).scale(COLLISION_DAMPING);
        } else if (speed.y() > 0 && getBounds().intersects(0, h, w, 100)) {
            pos = new Vec(pos.x(), h - getBounds().getBounds2D().getHeight() / 2.0);
            speed = new Vec(speed.x(), -speed.y()).scale(COLLISION_DAMPING);
        }
    }

    /**
     * Handles collisions with {@link Food} objects.
     */
    private void tickFoodCollisions() {
        simulation.getWorld().getFoodObjects().stream(false)
                .filter(food -> getBounds().intersects(food.getBounds().getBounds2D()))
                .forEach(food -> {
                    simulation.getWorld().getFoodObjects().remove(food);
                    energy += 100.0; // Fitness increases after eating food
                });
    }

    /**
     * Handles collisions with other individuals.
     */
    private void tickEnemyCollision() {
        eating = false; // First assume there is no collision with another individual

        if (wantToEat)
            simulation.getWorld().getIndividuals().stream(true)
                    .filter(ind -> ind != this && ind.getPos().sub(pos).squareLength() < Math.pow(halfTorsoLength + radius + ind.halfTorsoLength + ind.radius, 2.0)
                            && getBounds().intersects(ind.getBounds().getBounds2D())
                            && ind.getBounds().intersects(getBounds().getBounds2D()))
                    .findAny().ifPresent(target -> {
                        double rate = Math.min(EAT_RATE * energy, target.energy); // Cannot eat more than the remaining fitness
                        target.energy -= rate; // The target individual looses energy
                        energy += rate * EAT_EFFICIENCY; // This individual gains energy
                        eating = true;
                        target.beingEaten = true;
                    });
    }

    private void tickCollisions() {
        tickWallCollisions();
        tickFoodCollisions();
        tickEnemyCollision();
    }

    @Override
    public void tick() {
        age++;
        eye.tick();
        tickBrain();

        // Move
        super.tick();

        // Friction
        speed = speed.scale(TRANSLATIONAL_FRICTION);
        rotSpeed = rotSpeed.scale(ROTATIONAL_FRICTION);

        // Collisions
        tickCollisions();

        // Fitness decreases at each tick
        energy -= MOVING_COST * speed.squareLength()
                + ROTATION_COST * Math.pow(rotSpeed.normalized(), 2.0)
                + GENERAL_COST;

        if (energy < 0) {
            // Die if fitness is zero
            energy = 0.0;
            die();
            if (simulation.getWorld().getIndividuals().size() < simulation.getWorld().getMinPopulationSize())
                simulation.getWorld().addRandomIndividual();
        } else if (energy > MAX_ENERGY) {
            // Fitness cannot be larger than a certain value
            energy = MAX_ENERGY;
        }

        if (pos.isNaN())
            throw new IllegalStateException("Position of individual is NaN");
    }

    @Override
    public Shape makeBounds() {
        // The front and back circle of this individual
        var firstCircle = new Circle(pos.subX(halfTorsoLength), radius);
        var secondCircle = new Circle(pos.addX(halfTorsoLength), radius);

        // The rectangle combining the circles
        var rect = new Rect(pos.sub(halfTorsoLength, radius), new Vec(halfTorsoLength * 2.0, radius * 2.0));

        // Combine the two circles with the rectangle in the middle
        Area area = unionConvex(firstCircle, secondCircle, rect);

        // Rotate around center
        var transform = new AffineTransform();
        transform.rotate(rot.radians(), rect.getCenterX(), rect.getCenterY());
        return area.createTransformedArea(transform);
    }


    @Override
    public void render(Graphics2D g) {
        // The saturation depends on the fitness of this individual
        float[] hsv = RenderUtils.colorToHsv(color);
        hsv[1] = (float) Math.min(1.0, energy / 100.0) * 0.75f + 0.25f;
        Color renderColor = RenderUtils.hsvToColor(hsv);

        g.setColor(renderColor);
        g.fill(getBounds());

        // Border if eating or being eaten
        if (eating || beingEaten) {
            g.setColor(beingEaten ? Color.RED : Color.GREEN);
            g.setStroke(new BasicStroke(2f));
            g.draw(getBounds());
        }
        beingEaten = false;

        // Optionally render number of generation on each individual
        if (simulation.showGeneration()) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
            RenderUtils.drawCenteredString(g, String.format("%d", getGeneration()), pos);
        }
    }

    /**
     * Adds a zero mean gaussian noise to the hue value of this individual's color.
     *
     * @return a new mutated {@link Color} instance
     */
    private Color mutateColor() {
        float[] hsv = RenderUtils.colorToHsv(color);
        hsv[0] += simulation.getWorld().getRandom().nextGaussian(0.0, 0.01);
        hsv[0] %= 1.0;
        return RenderUtils.hsvToColor(hsv);
    }

    /**
     * Adds a zero mean gaussian noise to the mutation factor itself.
     *
     * @return a new, potentially slightly modified value for the mutation factor
     */
    private double mutateMutationFactor() {
        var rand = simulation.getWorld().getRandom();
        double res;
        do {
            res = mutationFactor + rand.nextGaussian(0.0, mutationFactor);
        } while (res < MIN_MUTATION_FACTOR);
        return res;
    }

    /**
     * Returns a new {@link Vec}tor with added zero mean gaussian noise.
     *
     * @param v      the vector to add random noise to
     * @param stddev the standard deviation
     * @return a new vector
     */
    private Vec addRandom(Vec v, double stddev) {
        return v.add(random.nextGaussian(0.0, stddev), random.nextGaussian(0.0, stddev));
    }

    /**
     * Returns a new {@link Rot}ation with added zero mean gaussian noise.
     *
     * @param r      the angle to add random noise to
     * @param stddev the standard deviation
     * @return a new {@link Rot} instance
     */
    private Rot addRandom(Rot r, double stddev) {
        return r.add(new Rot(random.nextGaussian(0.0, stddev)));
    }

    /**
     * Creates new individuals with mutated properties.
     *
     * @param numberOfChildren the number of child individuals to produce
     * @param force            an option to overwrite the potential preference of this individual not to reproduce
     */
    public void reproduce(int numberOfChildren, boolean force) {
        if (!force && !wantToReproduce)
            return;

        // Fitness splits between parent and children
        energy /= (numberOfChildren + 1);

        Set<Individual> mutations = new HashSet<>(numberOfChildren);
        for (int i = 0; i < numberOfChildren; i++) {
            var ind = new Individual(simulation,
                    addRandom(pos, halfTorsoLength), addRandom(speed, 0.1), // Slightly change position and speed
                    addRandom(rot, 0.1), addRandom(rotSpeed, 0.05), // Slightly change angle and rotational speed
                    mutateColor(), // Slightly change color
                    halfTorsoLength, energy, radius,
                    brain.mutate(mutationFactor), // IMPORTANT: Mutate the brain weights
                    mutateMutationFactor(), // Mutate the mutation factor itself
                    memory, generation + 1L);
            mutations.add(ind);
        }

        // Add children to simulation
        simulation.getWorld().getIndividuals().addAll(mutations);
    }

    /**
     * Removes this individual from the simulation.
     */
    public void die() {
        simulation.getWorld().getIndividuals().remove(this);
    }

    public double getEnergy() {
        return energy;
    }

    public long getGeneration() {
        return generation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        eye.setSimulation(simulation);
    }

    @Override
    public int compareTo(Individual o) {
        return Double.compare(energy, o.energy);
    }

    public long getAge() {
        return age;
    }
}
