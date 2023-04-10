package de.javaabc.aipopulation.util;

/**
 * A useful interface to force classes to have a tick method.
 *
 * @author Timo Friedl
 */
@FunctionalInterface
public interface Tickable {
    void tick();
}
