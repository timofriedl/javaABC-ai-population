package de.javaabc.aipopulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.RenderingHints.*;

public class Display extends JPanel implements KeyListener {
    private final Simulation simulation;

    public Display(Simulation simulation) {
        super(new BorderLayout());
        this.simulation = simulation;
        simulation.addKeyListener(this);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
        simulation.render(g2);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case ' ' -> simulation.setPause(!simulation.isPause());
            case 'b' -> simulation.toggleBest();
            case 'f' -> simulation.toggleFastForward();
            case 'g' -> simulation.toggleGeneration();
            case 'm' -> simulation.toggleMaxGeneration();
            case 'o' -> simulation.toggleOldest();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
