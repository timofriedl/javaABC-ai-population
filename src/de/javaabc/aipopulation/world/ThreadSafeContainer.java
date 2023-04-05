package de.javaabc.aipopulation.world;

import de.javaabc.aipopulation.objects.SimulationObject;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ThreadSafeContainer<T extends SimulationObject> implements Iterable<T>, Serializable {
    private final List<T> elements;

    public ThreadSafeContainer() {
        elements = new ArrayList<>();
    }

    public synchronized void add(T element) {
        elements.add(element);
    }

    public synchronized void addAll(Collection<T> elements) {
        elements.forEach(this::add);
    }

    public synchronized void remove(T element) {
        elements.remove(element);
    }

    @Override
    public Iterator<T> iterator() {
        var _this = this;
        return new Iterator<>() {
            int index = 0;
            T next = null;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;

                synchronized (_this) {
                    if (index < elements.size()) {
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

    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

    public void forEach(Consumer<? super T> action, boolean parallel) {
        stream(parallel).forEach(action);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        forEach(action, true);
    }

    public int size() {
        return elements.size();
    }
}
