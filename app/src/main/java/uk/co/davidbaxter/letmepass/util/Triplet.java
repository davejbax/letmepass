package uk.co.davidbaxter.letmepass.util;

/**
 * Immutable utility class like {@link android.util.Pair}, but for three item tuples.
 *
 * @param <T> First type
 * @param <U> Second type
 * @param <V> Third type
 */
public class Triplet<T, U, V> {

    public final T first;
    public final U second;
    public final V third;

    /**
     * Constructs a new triplet
     * @param first First value
     * @param second Second value
     * @param third Third value
     */
    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

}
