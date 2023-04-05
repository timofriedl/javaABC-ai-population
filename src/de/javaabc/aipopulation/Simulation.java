package de.javaabc.aipopulation;

import de.javaabc.aipopulation.geom.Circle;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.objects.Individual;
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

public class Simulation extends JFrame implements Tickable, Renderable {
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Font FONT;

    static {
        try {
            FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Simulation.class.getResourceAsStream("/fonts/JetBrains Mono/JetBrainsMono-VariableFont_wght.ttf")));
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Display display;

    private World world;

    private boolean pause;
    private boolean fastForward;
    private ScheduledExecutorService fastForwardService;

    private boolean showGeneration = true;
    private boolean showBest;
    private boolean showOldest;
    private boolean showMaxGeneration = true;
    private boolean showMaxGenerationCircle = true;

    public Simulation() {
        super("Simulation");

        setContentPane(display = new Display(this));
        setSize(SCREEN_SIZE);
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                world.save();
                System.exit(0);
            }
        });

        setVisible(true);
        init();
        runLoop();
    }

    private void init() {
        world = World.load(this)
                .orElseGet(() -> new World(this, 25, 100));
        world.initIndividuals();
        world.initFood();
    }

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
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(FONT);

        if (world == null)
            return;

        world.render(g);

        renderInfo(g);
    }

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

    public void toggleFastForward() {
        fastForward = !fastForward;

        if (fastForward) {
            fastForwardService = Executors.newSingleThreadScheduledExecutor();
            fastForwardService.scheduleWithFixedDelay(() -> {
                try {
                    for (int i = 0; i < 60; i++)
                        tick();
                    display.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0L, 1L, TimeUnit.NANOSECONDS);
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