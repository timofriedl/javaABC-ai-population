package de.javaabc.aipopulation;

import de.javaabc.aipopulation.geom.Circle;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.objects.Food;
import de.javaabc.aipopulation.objects.Individual;
import de.javaabc.aipopulation.objects.SimulationObject;
import de.javaabc.aipopulation.util.RenderUtils;
import de.javaabc.aipopulation.util.Renderable;
import de.javaabc.aipopulation.util.Tickable;
import de.javaabc.aipopulation.util.TimeUtil;
import de.javaabc.aipopulation.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * A genetic algorithm of bacteria-like {@link Individual}s that can move and rotate, eat {@link Food} or other individuals,
 * and reproduce themselves.
 *
 * @author Timo Friedl
 */
public class Simulation extends JFrame implements Tickable, Renderable {
    /**
     * the screen size of the main display device
     */
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * the display to render objects on
     */
    private final Display display;

    /**
     * the world containing all {@link SimulationObject}s
     */
    private World world;

    /**
     * a flag indicating that this simulation is currently paused
     */
    private boolean pause;

    /**
     * a flag indicating that this simulation is currently in fast-forward mode
     */
    private boolean fastForward;

    /**
     * the service for fast-forward mode
     */
    private ScheduledExecutorService fastForwardService;

    /**
     * a flag indicating that each individual should render its generation
     */
    private boolean showGeneration = true;

    /**
     * a flag indicating that the fittest individual should be highlighted
     */
    private boolean showBest;

    /**
     * a flag indicating that the oldest individual should be highlighted
     */
    private boolean showOldest;

    /**
     * a flag indicating that the highest generation should be rendered as a title on top
     */
    private boolean showMaxGeneration = true;

    /**
     * a flag indicating that the individual with the highest generation should be highlighted
     */
    private boolean showMaxGenerationCircle = true;

    /**
     * the text font
     */
    private Font font;

    /**
     * Creates a new simulation.
     */
    public Simulation() {
        super("Simulation"); // Create a new JFrame

        setContentPane(display = new Display(this));
        setSize(SCREEN_SIZE);
        setResizable(false);
        setUndecorated(true);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                world.save(); // Save before ALT+F4
                System.exit(0);
            }
        });

        initFont();
        setVisible(true);
        init();
        runLoop();
    }

    /**
     * Tries to load {@link Font} from disk or uses default font.
     */
    private void initFont() {
        try (var is = getClass().getResourceAsStream("/font/JetBrainsMono-VariableFont_wght.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
        } catch (NullPointerException | FontFormatException | IOException e) {
            e.printStackTrace();
            font = new Font("Arial", Font.PLAIN, 12);
        }
    }

    /**
     * Loads the {@link World} or creates a new one,
     * and initializes objects.
     */
    private void init() {
        world = World.load(this)
                .orElseGet(() -> new World(this, 25, 100));
        world.initIndividuals();
        world.initFood();
    }

    /**
     * Starts the simulation's tick-render-loop.
     * Each tick occurs approximately every 1/60 second.
     */
    private void runLoop() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                if (!pause && !fastForward) {
                    tick();
                    display.repaint();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3000L, 1000L * 1000L * 1000L / 60L, TimeUnit.NANOSECONDS);
    }

    @Override
    public void tick() {
        world.tick();
    }

    @Override
    public void render(Graphics2D g) {
        // Render background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFont(font);

        if (world == null)
            return;

        world.render(g);

        renderInfo(g);
    }

    /**
     * Renders additional information such as circles around best / oldest {@link Individual}
     * as well as the generation title.
     *
     * @param g the {@link Graphics2D} to draw on
     */
    private void renderInfo(Graphics2D g) {
        if (showBest)
            renderMaxIndividual(g, Comparator.comparingDouble(Individual::getEnergy),
                    new Color(0xff, 0x60, 0x00), best -> String.format("Energy: %d", Math.round(best.getEnergy())));

        if (showOldest)
            renderMaxIndividual(g, Comparator.comparingLong(Individual::getAge),
                    Color.DARK_GRAY, oldest -> "Age: " + TimeUtil.formatDuration(oldest.getAge()));

        if (showMaxGenerationCircle)
            world.getIndividuals().stream(true)
                    .max(Comparator.comparingLong(Individual::getGeneration))
                    .ifPresent(ind -> {
                        g.setStroke(new BasicStroke(4f));
                        g.setColor(new Color(255, 0, 40));
                        g.draw(new Circle(ind.getPos(), 50.0));
                    });

        if (showMaxGeneration) {
            String text = "Generation " + world.getIndividuals().stream(true)
                    .mapToLong(Individual::getGeneration)
                    .max().orElse(0L);

            Vec pos = new Vec(getWidth() / 2.0, getHeight() / 16.0);
            g.setColor(new Color(0, 0, 0, 0xa0));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 60f));
            RenderUtils.drawCenteredString(g, text, pos);
        }
    }

    /**
     * Renders a circle around a somehow maximal individual.
     *
     * @param g              the {@link Graphics2D} to draw on
     * @param criteria       a {@link Comparator} defining which of two individuals is higher in order
     * @param color          the color of the circle (and the text next to it)
     * @param labelExtractor a {@link Function} that maps the highlighted individual to the text to show next to it
     */
    private void renderMaxIndividual(Graphics2D g, Comparator<Individual> criteria, Color color, Function<Individual, String> labelExtractor) {
        world.getIndividuals().stream(true)
                .max(criteria)
                .ifPresent(max -> {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(4f));
                    Vec pos = max.getPos();
                    Vec textPos = pos.add(40.0, -40.0)
                            .restrict(50.0, 50.0, getWidth() - 200.0, getHeight() - 50.0);
                    g.draw(new Circle(pos, 50.0));
                    g.setFont(g.getFont().deriveFont(20f));
                    g.drawString(labelExtractor.apply(max), (float) textPos.x(), (float) textPos.y());
                });
    }

    /**
     * Switches from normal mode to fast-forward mode or vice versa.
     */
    public void toggleFastForward() {
        fastForward = !fastForward;

        if (fastForward) {
            fastForwardService = Executors.newSingleThreadScheduledExecutor();
            fastForwardService.scheduleWithFixedDelay(() -> {
                try {
                    for (int i = 0; i < 60; i++)
                        tick();
                    display.repaint(); // Render after 60 ticks instead of every tick
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0L, 1L, TimeUnit.NANOSECONDS); // Do not execute every 1/60 second, but as fast as possible instead
        } else {
            fastForwardService.shutdown();
        }
    }

    public void toggleGeneration() {
        showGeneration = !showGeneration;
    }

    public void toggleBest() {
        showBest = !showBest;
    }

    public void toggleOldest() {
        showOldest = !showOldest;
    }

    public void toggleMaxGeneration() {
        showMaxGeneration = !showMaxGeneration;
        showMaxGenerationCircle = !showMaxGenerationCircle;
    }

    public static void main(String[] args) {
        new Simulation();
    }

    public World getWorld() {
        return world;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isPause() {
        return pause;
    }

    public boolean showGeneration() {
        return showGeneration;
    }
}