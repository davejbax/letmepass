package uk.co.davidbaxter.letmepass.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Future} implementation representing a Future that has a value that can be accessed
 * instantly, i.e. one that is complete upon construction. This is useful for APIs where Futures
 * must be returned.
 *
 * @param <T> Type of value that is held in this Future
 */
public class InstantFuture<T> implements Future<T> {

    private final T value;

    /**
     * Constructs a new InstantFuture that will return the given value
     * @param value Value to return in Future
     */
    public InstantFuture(T value) {
        this.value = value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // Task has completed: we cannot cancel
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T get(long timeout, @NonNull TimeUnit unit) {
        return value;
    }
}
