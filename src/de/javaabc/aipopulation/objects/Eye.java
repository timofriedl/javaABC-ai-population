package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.Simulation;
import de.javaabc.aipopulation.geom.Line;
import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.util.Tickable;

import java.awt.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class Eye implements Tickable, Serializable {
    private transient Simulation simulation;
    private final MovableObject owner;

    // Food
    private transient Optional<Food> foodTarget;
    private Rot detectedFoodRotation = Rot.ZERO;
    private double detectedFoodSqDistance = Double.MAX_VALUE;

    // Enemys
    private transient Optional<Individual> enemyTarget;
    private Rot detectedEnemyRotation = Rot.ZERO;
    private double detectedEnemySqDistance = Double.MAX_VALUE;
    private double detectedEnemyHue = -1.0;
    private double detectedEnemySaturation = -1.0;

    public Eye(Simulation simulation, MovableObject owner) {
        this.simulation = simulation;
        this.owner = owner;
        this.foodTarget = Optional.empty();
        this.enemyTarget = Optional.empty();
    }

    @Override
    public void tick() {
        foodTarget = simulation.getWorld().getFoodObjects().stream(false)
                .map(food -> Map.entry(food, food.getPos().sub(owner.getPos()).squareLength()))
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);

        foodTarget.ifPresentOrElse(food -> {
            detectedFoodSqDistance = food.getPos().sub(owner.getPos()).squareLength();
            detectedFoodRotation = food.getPos().sub(owner.getPos()).angle().sub(owner.getRot());
        }, () -> {
            detectedFoodSqDistance = Double.MAX_VALUE;
            detectedFoodRotation = Rot.ZERO;
        });

        enemyTarget = simulation.getWorld().getIndividuals().stream(false)
                .filter(ind -> ind != owner)
                .map(ind -> Map.entry(ind, ind.getPos().sub(owner.getPos()).squareLength()))
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);

        enemyTarget.ifPresentOrElse(enemy -> {
            detectedEnemySqDistance = enemy.getPos().sub(owner.getPos()).squareLength();
            detectedEnemyRotation = enemy.getPos().sub(owner.getPos()).angle().sub(owner.getRot());
            float[] hsv = new float[3];
            Color.RGBtoHSB(enemy.color.getRed(), enemy.color.getGreen(), enemy.color.getBlue(), hsv);
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
        g.setColor(Color.DARK_GRAY);
        foodTarget.ifPresent(food -> g.draw(new Line(owner.getPos(), food.getPos())));
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
