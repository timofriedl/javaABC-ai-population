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

public class Individual extends MovableObject implements Comparable<Individual>, Serializable {
    private static final double MAX_ACC = 0.3;
    private static final double MAX_ROT_ACC = 0.02;
    private static final double TRANSLATIONAL_FRICTION = 0.9;
    private static final double ROTATIONAL_FRICTION = 0.7;
    private static final double COLLISION_DAMPING = 0.5;
    private static final double MOVING_COST = 0.001;
    private static final double ROTATION_COST = 1000.0;
    private static final double GENERAL_COST = 0.002;
    private static final double EAT_RATE = 0.005;
    private static final double EAT_EFFICIENCY = 0.5;

    protected transient Simulation simulation;
    protected final double radius;
    protected transient Shape[] shapes;
    protected final double length;
    protected double energy;

    protected NeuralNetwork brain;

    protected Eye eye;
    protected final double mutationFactor;

    // Statistics
    protected String originId;
    protected long age;
    protected double[] memory;

    protected boolean wantToEat;
    protected boolean eating;
    protected boolean beingEaten;

    protected boolean wantToReproduce;

    public Individual(Simulation simulation, Vec pos, Vec speed, Rot rot, Rot rotSpeed, Color color, double length, double energy,
                      double radius, NeuralNetwork brain, String orignId, double mutationFactor, double[] memory) {
        super(pos, speed, rot, rotSpeed, color);
        this.simulation = simulation;
        this.length = length;
        this.energy = energy;
        this.radius = radius;
        this.brain = brain;
        this.originId = orignId;
        this.mutationFactor = mutationFactor;
        this.memory = Arrays.copyOf(memory, memory.length);

        eye = new Eye(simulation, this);
    }

    public Individual(Simulation simulation, Vec pos, Rot rot, Color color, double length, double radius) {
        this(simulation, pos, Vec.ZERO, rot, Rot.ZERO, color, length, 100.0, radius,
                new NeuralNetwork(simulation.getWorld().getRandom(), 12 + 5, 12, 12, 4 + 5),
                "", 1E-2, new double[5]
        );
    }

    private void tickBrain() {
        double[] inputs = {
                energy / 100.0,
                pos.x() / simulation.getWidth(),
                pos.y() / simulation.getHeight(),
                speed.x(),
                speed.y(),
                rotSpeed.normalized(),
                eye.getDetectedFoodRotation().normalized(),
                10_000.0 / eye.getDetectedFoodSqDistance(),
                eye.getDetectedEnemyRotation().normalized(),
                10_000.0 / eye.getDetectedEnemySqDistance(),
                eye.getDetectedEnemyHue(),
                eye.getDetectedEnemySaturation(),
                memory[0],
                memory[1],
                memory[2],
                memory[3],
                memory[4]
        };

        double[] output = brain.feedForward(inputs);
        memory = Arrays.copyOfRange(output, output.length - memory.length, output.length);

        acc = Vec.unit(rot).scale(output[0] * MAX_ACC);
        rotAcc = new Rot(output[1] * MAX_ROT_ACC);
        wantToEat = output[2] > 0.0;
        wantToReproduce = output[3] > 0.0;
    }

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

    private void tickFoodCollisions() {
        simulation.getWorld().getFoodObjects().stream(false)
                .filter(food -> getBounds().intersects(food.getBounds().getBounds2D()))
                .forEach(food -> {
                    simulation.getWorld().getFoodObjects().remove(food);
                    energy += 100.0;
                });
    }

    private void tickEnemyCollision() {
        eating = false;

        if (wantToEat)
            simulation.getWorld().getIndividuals().stream(true)
                    .filter(ind -> ind.getPos().sub(pos).squareLength() < Math.pow(length + radius + ind.length + ind.radius, 2.0)
                            && getBounds().intersects(ind.getBounds().getBounds2D())
                            && ind.getBounds().intersects(getBounds().getBounds2D()))
                    .findAny().ifPresent(target -> {
                        double rate = Math.min(EAT_RATE * energy, target.energy);
                        target.energy -= rate;
                        energy += rate * EAT_EFFICIENCY;
                        eating = true;
                        target.beingEaten = true;
                    });
    }

    protected void tickCollisions() {
        tickWallCollisions();
        tickFoodCollisions();
        tickEnemyCollision();
    }

    @Override
    public void tick() {
        age++;

        eye.tick();
        tickBrain();
        super.tick();
        speed = speed.scale(TRANSLATIONAL_FRICTION);
        rotSpeed = rotSpeed.scale(ROTATIONAL_FRICTION);

        tickCollisions();

        energy -= MOVING_COST * speed.squareLength()
                + ROTATION_COST * Math.pow(rotSpeed.normalized(), 2.0)
                + GENERAL_COST;
        if (energy < 0) {
            energy = 0.0;
            die();
            if (simulation.getWorld().getIndividuals().size() < simulation.getWorld().getMinPopulationSize())
                simulation.getWorld().addRandomIndividual();
        } else if (energy > 200.0)
            energy = 200.0;

        if (Double.isNaN(pos.x()) || Double.isNaN(pos.y()))
            throw new IllegalStateException("Position of individual is NaN");
    }

    @Override
    public Shape makeBounds() {
        var firstCircle = new Circle(pos.subX(length), radius);
        var secondCircle = new Circle(pos.addX(length), radius);
        var rect = new Rect(pos.sub(length, radius), new Vec(length * 2.0, radius * 2.0));

        shapes = new Shape[]{firstCircle, secondCircle, rect};

        // Combine the two circles with the rectangle in the middle
        Area area = unionConvex(shapes);

        // Rotate around center
        var transform = new AffineTransform();
        transform.rotate(rot.radians(), rect.getCenterX(), rect.getCenterY());
        return area.createTransformedArea(transform);
    }


    @Override
    public void render(Graphics2D g) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
        hsv[1] = (float) Math.min(1.0, energy / 100.0) * 0.75f + 0.25f;
        Color renderColor = new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));

        g.setColor(renderColor);
        g.fill(getBounds());

        if (eating || beingEaten) {
            g.setColor(eating ? Color.GREEN : Color.RED);
            g.setStroke(new BasicStroke(2f));
            g.draw(getBounds());
        }
        beingEaten = false;

        if (simulation.showGeneration()) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
            RenderUtils.drawCenteredString(g, String.format("%d", getGeneration()), pos);
        }
    }

    private Color mutateColor() {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[0] += simulation.getWorld().getRandom().nextGaussian(0.0, 0.01);
        hsb[0] %= 1.0;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    private double mutateMutationFactor() {
        var rand = simulation.getWorld().getRandom();
        double res;
        do {
            res = mutationFactor + rand.nextGaussian(0.0, mutationFactor);
        } while (res < 1E-4);
        return res;
    }

    public void reproduce(int numberOfChildren, boolean force) {
        if (!force && !wantToReproduce)
            return;

        Random rand = simulation.getWorld().getRandom();
        energy /= (numberOfChildren + 1);

        Set<Individual> mutations = new HashSet<>(4);
        for (int i = 0; i < numberOfChildren; i++)
            mutations.add(new Individual(simulation,
                    pos.add(rand.nextGaussian(0.0, length), rand.nextGaussian(0.0, length)),
                    speed.add(rand.nextGaussian(0.0, 0.1), rand.nextGaussian(0.0, 0.1)),
                    rot.add(new Rot(rand.nextGaussian(0.0, 0.1))),
                    rotSpeed.add(new Rot(rand.nextGaussian(0.0, 0.1))),
                    mutateColor(), length, energy, radius, brain.mutate(mutationFactor),
                    originId + i, mutateMutationFactor(), memory));

        pos = pos.addX(rand.nextGaussian(0.0, length));
        simulation.getWorld().getIndividuals().addAll(mutations);
    }

    public void die() {
        simulation.getWorld().getIndividuals().remove(this);
    }

    public double getEnergy() {
        return energy;
    }

    public long getGeneration() {
        return originId.length();
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
