package uk.co.davidbaxter.letmepass.util;

/**
 * A consumer of a result from some operation that may fail with an exception.
 * <p>
 * Note that typically, Consumers will only be called once per operation that they are used in.
 *
 * @param <T> Type of result returned from some operation
 */
public interface Consumer<T> {

    /**
     * Accepts a result from the operation for which this Consumer was used
     * @param t Operation result
     */
    void accept(T t);

    /**
     * Accepts an exception from the operation for which this Consumer was used.
     * @param t Throwable from the operation
     */
    void onFailure(Throwable t);

}
