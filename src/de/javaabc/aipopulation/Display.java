package de.javaabc.aipopulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.RenderingHints.*;

/**
 * A {@link JPanel} to render the simulation on.
 *
 * @author Timo Friedl
 */
public class Display extends JPanel implements KeyListener {
    /**
     * the reference to the main {@link Simulation} instance
     */
    private final Simulation simulation;

    /**
     * Creates a new display.
     *
     * @param simulation the reference to the main {@link Simulation} instance
     */
    public Display(Simulation simulation) {
        super(new BorderLayout());
        this.simulation = simulation;
        simulation.addKeyListener(this);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Improve render quality
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);

        // Render everything
        simulation.render(g2);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // ignore
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case ' ' -> simulation.setPause(!simulation.isPause()); // Pause or resume simulation
            case 'b' -> simulation.toggleBest(); // Toggle show the fittest individual
            case 'f' -> simulation.toggleFastForward(); // Switch between fast-forward and normal
            case 'g' -> simulation.toggleGeneration(); // Toggle show generation of each individual
            case 'm' -> simulation.toggleMaxGeneration(); // Toggle show individual with the highest generation
            case 'o' -> simulation.toggleOldest(); // Toggle show the oldest individual
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // ignore
    }
}
