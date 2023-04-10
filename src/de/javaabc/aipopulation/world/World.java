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
import java.util.Optional;
import java.util.Random;

/**
 * The simulation world.
 *
 * @author Timo Friedl
 */
public class World implements Tickable, Renderable, Serializable {
    /**
     * the save directory
     */
    private static final String PATH_DIR = System.getProperty("user.home") + File.separator + ".aipopulation" + File.separator;

    /**
     * the world save path
     */
    private static final String WORLD_PATH = PATH_DIR + "world.txt";

    /**
     * the reference to the main {@link Simulation} instance
     */
    private transient Simulation simulation;

    /**
     * Lower and upper bounds to the population size.
     * If there are fewer individuals than min, new ones are added.
     * If there are more individuals than max, the least fit individuals die.
     */
    private final int minPopulationSize, maxPopulationSize;

    /**
     * the individuals in this world
     */
    private final ThreadSafeContainer<Individual> individuals;

    /**
     * the food objects in this world
     */
    private final ThreadSafeContainer<Food> foodObjects;

    /**
     * a pseudorandom number generator for object initialization
     */
    private final Random random;

    /**
     * the number of ticks in this world so far
     */
    private long totalTicks;

    /**
     * Creates a new world with given properties.
     *
     * @param simulation        the reference to the main {@link Simulation} instance
     * @param minPopulationSize the minimum number of individuals in this world
     * @param maxPopulationSize the maximum number of individuals in this world
     * @param individuals       the individuals in this world
     * @param foodObjects       the food objects in this world
     * @param totalTicks        the number of ticks in this world so far
     */
    private World(Simulation simulation, int minPopulationSize, int maxPopulationSize, ThreadSafeContainer<Individual> individuals, ThreadSafeContainer<Food> foodObjects, long totalTicks) {
        this.simulation = simulation;
        this.minPopulationSize = minPopulationSize;
        this.maxPopulationSize = maxPopulationSize;
        this.individuals = individuals;
        this.foodObjects = foodObjects;
        this.totalTicks = totalTicks;
        random = new Random();
    }

    /**
     * Creates a new empty world.
     *
     * @param simulation        the reference to the main {@link Simulation} instance
     * @param minPopulationSize the minimum number of individuals in this world
     * @param maxPopulationSize the maximum number of individuals in this world
     */
    public World(Simulation simulation, int minPopulationSize, int maxPopulationSize) {
        this(simulation, minPopulationSize, maxPopulationSize, new ThreadSafeContainer<>(), new ThreadSafeContainer<>(), 0L);
    }

    /**
     * Loads a {@link World} from disk.
     *
     * @param simulation the reference to the main {@link Simulation} instance
     * @return an {@link Optional} containing the loaded world, or an empty optional if failed to load
     */
    public static Optional<World> load(Simulation simulation) {
        try (FileInputStream fis = new FileInputStream(WORLD_PATH);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            var world = (World) ois.readObject();
            world.simulation = simulation;
            world.getIndividuals().forEach(ind -> ind.setSimulation(simulation));
            return Optional.of(world);
        } catch (FileNotFoundException e) {
            System.err.print("Failed to load world. Creating new.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Saves this world to disk.
     */
    public void save() {
        simulation.setPause(true);

        var dir = new File(PATH_DIR);
        if (dir.exists() || dir.mkdirs()) {
            System.out.println("Saving \"" + WORLD_PATH + "\"...");
            try (FileOutputStream fos = new FileOutputStream(WORLD_PATH);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(this);
                System.out.println("...done!");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                simulation.setPause(false);
            }
        }
    }

    /**
     * Adds a few randomly initialized {@link Individual}s to this world.
     */
    public void initIndividuals() {
        if (individuals.size() > 0)
            return;

        for (int i = 0; i < (minPopulationSize + maxPopulationSize) / 2; i++)
            addRandomIndividual();
    }

    /**
     * Adds a randomly initialized {@link Individual} to this world.
     */
    public void addRandomIndividual() {
        int length = 10;
        int radius = 10;
        int border = radius + length;
        Color blue = new Color(0x00, 0x80, 0xFF);

        int x = random.nextInt(simulation.getWidth() - 2 * border) + border;
        int y = random.nextInt(simulation.getHeight() - 2 * border) + border;
        Rot rot = Rot.norm(Math.random());
        individuals.add(new Individual(simulation, new Vec(x, y), rot, blue, radius));
    }

    /**
     * Adds a random {@link Food} object for each {@link Individual} in this world.
     */
    public void initFood() {
        for (int i = 0; i < individuals.size(); i++)
            addRandomFood();
    }

    /**
     * Adds a random {@link Food} object to this world.
     */
    public void addRandomFood() {
        int x = random.nextInt(simulation.getWidth());
        int y = random.nextInt(simulation.getHeight());
        foodObjects.add(new Food(new Vec(x, y)));
    }

    /**
     * @return the maximum number of generations some {@link Individual} has in this world
     */
    public long getMaxGeneration() {
        return individuals.stream(true)
                .mapToLong(Individual::getGeneration)
                .max().orElse(0L);
    }

    @Override
    public void tick() {
        totalTicks++;
        if (totalTicks % (5 * 60 * 60) == 0) {
            // Print info and save to disk
            System.out.println("tick " + totalTicks + " (" + (totalTicks / 3600) + "min). Generations: " + getMaxGeneration());
            System.out.println(individuals.size() + " individuals total");
            save();
        }

        // Spawn food every 5 seconds
        if (totalTicks % (5 * 60) == 0 && individuals.size() < maxPopulationSize)
            addRandomFood();

        // Tick individuals
        individuals.forEach(Individual::tick);

        // Reproduce individuals
        individuals.stream(true)
                .filter(ind -> ind.getEnergy() >= 100.0)
                .forEach(ind -> ind.reproduce(2, false));

        // Manage over- / underpopulation
        if (individuals.size() > maxPopulationSize)
            purge();
        else if (individuals.size() < minPopulationSize)
            forceReproduction();
    }

    /**
     * Kill {@link Individual}s to match {@link #maxPopulationSize}
     */
    private void purge() {
        individuals.stream(true)
                .sorted(Individual::compareTo)
                .limit(individuals.size() - maxPopulationSize)
                .forEach(individuals::remove);
    }

    /**
     * Force fit {@link Individual}s to reproduce in order to match {@link #minPopulationSize}
     */
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
