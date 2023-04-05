package de.javaabc.aipopulation.world;

import de.javaabc.aipopulation.Simulation;
import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.objects.Food;
import de.javaabc.aipopulation.objects.Individual;
import de.javaabc.aipopulation.util.Renderable;
import de.javaabc.aipopulation.util.Tickable;

import java.awt.*;
import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class World implements Tickable, Renderable, Serializable {
    private transient Simulation simulation;

    private final int minPopulationSize, maxPopulationSize;

    private final ThreadSafeContainer<Individual> individuals;
    private final ThreadSafeContainer<Food> foodObjects;

    private final Random random;
    private long totalTicks;

    private World(Simulation simulation, int minPopulationSize, int maxPopulationSize, ThreadSafeContainer<Individual> individuals, ThreadSafeContainer<Food> foodObjects, Random random, long totalTicks) {
        this.simulation = simulation;
        this.minPopulationSize = minPopulationSize;
        this.maxPopulationSize = maxPopulationSize;
        this.individuals = individuals;
        this.foodObjects = foodObjects;
        this.random = random;
        this.totalTicks = totalTicks;
    }

    public World(Simulation simulation, int minPopulationSize, int maxPopulationSize) {
        this(simulation, minPopulationSize, maxPopulationSize, new ThreadSafeContainer<>(), new ThreadSafeContainer<>(), new Random(0L), 0L);
    }

    public static Optional<World> load(Simulation simulation) {
        try (FileInputStream fis = new FileInputStream(Objects.requireNonNull(World.class.getResource("/worlds/default_world.txt")).getFile());
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            var world = (World) ois.readObject();
            world.simulation = simulation;
            world.getIndividuals().forEach(ind -> ind.setSimulation(simulation));
            return Optional.of(world);
        } catch (EOFException | InvalidClassException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void save() {
        String path = Objects.requireNonNull(getClass().getResource("/worlds/default_world.txt")).getFile();
        System.out.println("Saving \"" + path + "\"...");
        simulation.setPause(true);
        try (FileOutputStream fos = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
            System.out.println("...done!");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            simulation.setPause(false);
        }
    }

    public void initIndividuals() {
        if (individuals.size() > 0)
            return;

        for (int i = 0; i < (minPopulationSize + maxPopulationSize) / 2; i++)
            addRandomIndividual();
    }

    public void addRandomIndividual() {
        int length = 10;
        int radius = 10;
        int border = radius + length;
        Color blue = new Color(0x00, 0x80, 0xFF);

        int x = random.nextInt(simulation.getWidth() - 2 * border) + border;
        int y = random.nextInt(simulation.getHeight() - 2 * border) + border;
        Rot rot = Rot.norm(Math.random());
        individuals.add(new Individual(simulation, new Vec(x, y), rot, blue, length, radius));
    }

    public void initFood() {
        for (int i = 0; i < individuals.size(); i++)
            addRandomFood();
    }

    public void addRandomFood() {
        int x = random.nextInt(simulation.getWidth());
        int y = random.nextInt(simulation.getHeight());
        foodObjects.add(new Food(new Vec(x, y)));
    }

    public long getMaxGeneration() {
        return individuals.stream(true)
                .mapToLong(Individual::getGeneration)
                .max().orElse(0L);
    }

    @Override
    public void tick() {
        totalTicks++;
        if (totalTicks % 36000 == 0) {
            System.out.println("tick " + totalTicks + " (" + (totalTicks / 3600) + "min). Generations: " + getMaxGeneration());
            System.out.println(individuals.size() + " individuals total");
            save();
        }

        if (totalTicks % (5 * 60) == 0 && individuals.size() < maxPopulationSize)
            spawnFood();

        individuals.forEach(Individual::tick);

        individuals.stream(true)
                .filter(ind -> ind.getEnergy() >= 100.0)
                .forEach(ind -> ind.reproduce(2, false));

        if (individuals.size() > maxPopulationSize)
            purge();
        else if (individuals.size() < minPopulationSize)
            forceReproduction();
    }

    private void spawnFood() {
        double cx = random.nextDouble() * simulation.getWidth();
        double cy = random.nextDouble() * simulation.getHeight();
        foodObjects.add(new Food(new Vec(cx, cy)));
    }

    private void purge() {
        individuals.stream(true)
                .sorted(Individual::compareTo)
                .limit(individuals.size() - maxPopulationSize)
                .forEach(individuals::remove);
    }

    private void forceReproduction() {
        individuals.stream(true)
                .max(Individual::compareTo)
                .ifPresent(ind -> ind.reproduce(minPopulationSize - individuals.size(), true));
    }

    @Override
    public void render(Graphics2D g) {
        foodObjects.forEach(food -> food.render(g));
        individuals.forEach(individual -> individual.render(g), false);
    }


    public Random getRandom() {
        return random;
    }

    public ThreadSafeContainer<Individual> getIndividuals() {
        return individuals;
    }

    public ThreadSafeContainer<Food> getFoodObjects() {
        return foodObjects;
    }

    public int getMinPopulationSize() {
        return minPopulationSize;
    }
}
