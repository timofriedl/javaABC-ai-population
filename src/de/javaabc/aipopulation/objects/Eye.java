package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.Simulation;
import de.javaabc.aipopulation.geom.Line;
import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.RenderUtils;
import de.javaabc.aipopulation.util.Tickable;

import java.awt.*;
import java.io.Serializable;
import java.util.Optional;

/**
 * The visual system of an {@link Individual}.
 * <p>
 * The eye detects the closest {@link Food} object as well as the closest other {@link Individual}.
 * More specifically, the following data is gathered:
 * - The direction and (squared) distance to the detected closest food object
 * - The direction and (squared) distance to the detected closest other individual
 * - Both hue and saturation value of the color of the detected closest other individual
 *
 * @author Timo Friedl
 */
public class Eye implements Tickable, Serializable {
    /**
     * A reference to the main {@link Simulation}.
     */
    private transient Simulation simulation;

    /**
     * the {@link Individual} owning this eye
     */
    private final MovableObject owner;

    /**
     * the detected closest food object, if any
     */
    private transient Optional<Food> foodTarget;

    /**
     * the rotation difference between this eye's owner and the detected food object
     */
    private Rot detectedFoodRotation = Rot.ZERO;

    /**
     * the squared distance to the detected food object
     */
    private double detectedFoodSqDistance = Double.MAX_VALUE;

    /**
     * the detected closest other individual, if any
     */
    private transient Optional<Individual> enemyTarget;

    /**
     * the rotation difference between this eye's owner and the detected closest other individual
     */
    private Rot detectedEnemyRotation = Rot.ZERO;

    /**
     * the squared distance to the detected closest other individual
     */
    private double detectedEnemySqDistance = Double.MAX_VALUE;

    /**
     * the hue value of the detected closest other individual's color
     */
    private double detectedEnemyHue = -1.0;

    /**
     * the saturation value of the detected closest other individual's color
     */
    private double detectedEnemySaturation = -1.0;

    /**
     * Creates a new visual system for a certain individual.
     *
     * @param simulation the reference to the main {@link Simulation}
     * @param owner      the {@link Individual} owning this eye
     */
    public Eye(Simulation simulation, MovableObject owner) {
        this.simulation = simulation;
        this.owner = owner;
        this.foodTarget = Optional.empty();
        this.enemyTarget = Optional.empty();
    }

    @Override
    public void tick() {
        // Food
        foodTarget = owner.findClosest(simulation.getWorld().getFoodObjects());
        foodTarget.ifPresentOrElse(food -> {
            Vec distance = owner.vectorTo(food);
            detectedFoodSqDistance = distance.squareLength();
            detectedFoodRotation = distance.angle().sub(owner.getRot());
        }, () -> {
            detectedFoodSqDistance = Double.MAX_VALUE;
            detectedFoodRotation = Rot.ZERO;
        });

        // Other individual
        enemyTarget = owner.findClosest(simulation.getWorld().getIndividuals());
        enemyTarget.ifPresentOrElse(enemy -> {
            Vec distance = owner.vectorTo(enemy);
            detectedEnemySqDistance = distance.squareLength();
            detectedEnemyRotation = distance.angle().sub(owner.getRot());

            float[] hsv = RenderUtils.colorToHsv(enemy.color);
            detectedEnemyHue = hsv[0];
            detectedEnemySaturation = hsv[1];
        }, () -> {
            detectedEnemySqDistance = Double.MAX_VALUE;
            detectedEnemyRotation = Rot.ZERO;

            detectedEnemyHue = -1.0;
            detectedEnemySaturation = -1.0;
        });
    }

    @Deprecated
    public void render(Graphics2D g) {
        g.setStroke(new BasicStroke(2f));

        // Line to the closest food
        g.setColor(Color.DARK_GRAY);
        foodTarget.ifPresent(food -> g.draw(new Line(owner.getPos(), food.getPos())));

        // Line to the closest other individual
        g.setColor(owner.getColor());
        enemyTarget.ifPresent(enemy -> g.draw(new Line(owner.getPos(), enemy.getPos())));
    }

    public Rot getDetectedFoodRotation() {
        return detectedFoodRotation;
    }

    public double getDetectedFoodSqDistance() {
        return detectedFoodSqDistance;
    }

    public Rot getDetectedEnemyRotation() {
        return detectedEnemyRotation;
    }

    public double getDetectedEnemySqDistance() {
        return detectedEnemySqDistance;
    }

    public double getDetectedEnemyHue() {
        return detectedEnemyHue;
    }

    public double getDetectedEnemySaturation() {
        return detectedEnemySaturation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }
}
