package de.javaabc.aipopulation.util;

import java.awt.*;

/**
 * A useful interface to force classes to have a render method.
 *
 * @author Timo Friedl
 */
@FunctionalInterface
public interface Renderable {
    void render(Graphics2D g);
}
