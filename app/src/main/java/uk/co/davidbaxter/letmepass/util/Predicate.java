package uk.co.davidbaxter.letmepass.util;

/**
 * Utility interface for a function that returns a boolean based on a value
 * @param <T> Type of value to test
 */
public interface Predicate<T> {

    /**
     * Tests a value
     * @param t Value
     * @return The result of testing the value
     */
    boolean test(T t);

}
