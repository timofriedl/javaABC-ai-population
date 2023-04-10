package de.javaabc.aipopulation.world;

import de.javaabc.aipopulation.objects.SimulationObject;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A container of elements that can be accessed by multiple threads at once.
 *
 * @param <T> the type of {@link SimulationObject} this container contains
 * @author Timo Friedl
 */
public class ThreadSafeContainer<T extends SimulationObject> implements Iterable<T>, Serializable {
    /**
     * the {@link List} of elements in this container
     */
    private final List<T> elements;

    /**
     * Creates a new empty thread safe container.
     */
    public ThreadSafeContainer() {
        elements = new ArrayList<>();
    }

    /**
     * Adds an element to this container.
     *
     * @param element the element to add
     */
    public synchronized void add(T element) {
        elements.add(element);
    }

    /**
     * Adds multiple elements to this container.
     *
     * @param elements the collection of elements to add
     */
    public synchronized void addAll(Collection<T> elements) {
        elements.forEach(this::add);
    }

    /**
     * Removes a given element from this container.
     *
     * @param element the element to remove
     */
    public synchronized void remove(T element) {
        elements.remove(element);
    }

    /**
     * Creates a new {@link Iterator} instance that iterates through the elements of this container.
     * Warning: Elements might be skipped on concurrent removal, or returned even after removal.
     *
     * @return a new {@link Iterator} on this collection
     */
    @Override
    public Iterator<T> iterator() {
        var _this = this; // Reference to the ThreadSafeContainer instance
        return new Iterator<>() {
            int index = 0; // Start at first element
            T next = null;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true; // Already has next element

                synchronized (_this) {
                    if (index < elements.size()) {
                        // Fetch next element
                        next = elements.get(index++);
                        return true;
                    }
                }

                return false;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                T res = next;
                next = null;
                return res;
            }
        };
    }

    /**
     * Streams the elements of this container.
     *
     * @param parallel the option to return a parallel {@link Stream}
     * @return a new {@link Stream} on this container
     */
    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

    /**
     * Applies a given action to the elements of this container.
     *
     * @param action   the action to apply
     * @param parallel the option to apply the action in a parallel manner
     */
    public void forEach(Consumer<? super T> action, boolean parallel) {
        stream(parallel).forEach(action);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        forEach(action, true);
    }

    /**
     * @return the number of elements in this container
     */
    public int size() {
        return elements.size();
    }
}
