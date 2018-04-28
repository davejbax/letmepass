package uk.co.davidbaxter.letmepass.util;

import android.arch.core.util.Function;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Algorithms {

    private static final int LEV_THRESHOLD_HIGH = 6;
    private static final int LEV_THRESHOLD_LOW = 2;
    private static final LevenshteinDistance LEV_LIMITED_LOW
            = new LevenshteinDistance(LEV_THRESHOLD_LOW);
    private static final LevenshteinDistance LEV_LIMITED_HIGH
            = new LevenshteinDistance(LEV_THRESHOLD_HIGH);
    private static final LevenshteinDistance LEV_UNLIMITED = new LevenshteinDistance();

    /**
     * Performs a Levenshtein distance calculation with no limit to the distance between L & R.
     * @param left First string to compare
     * @param right Second string to compare
     * @return The Levenshtein distance between the strings
     */
    public static int levUnlimited(String left, String right) {
        return LEV_UNLIMITED.apply(left, right);
    }

    /**
     * Performs a limited Levenshtein distance calculation, returning -1 if the distance exceeds
     * the set limit. There are two set limits: one high, one low. Setting `lowLimit` to true will
     * select the lower limit.
     *
     * @param left First string to compare
     * @param right Second string to compare
     * @param lowLimit True to use the lowest threshold; false to use the highest threshold
     * @return Levenshtein distance, or -1 if the threshold was exceeded
     */
    public static int levLimited(String left, String right, boolean lowLimit) {
        if (lowLimit)
            return LEV_LIMITED_LOW.apply(left, right);
        else
            return LEV_LIMITED_HIGH.apply(left, right);
    }

    /**
     * Performs a breadth first search, stopping at the first matching result.
     *
     * @param list List to search
     * @param isMatching A predicate to invoke to determine whether an item in the list matches
     *                   some criteria
     * @param getChildren A function to get the children of an item in the list, if there are any.
     *                    If there are no children, the function should return an empty list.
     * @param <T> The type of item to search for
     * @return The first found matching result, or null if there were none.
     */
    public static <T> T breadthFirstSearch(List<T> list,
                                    Predicate<T> isMatching,
                                    Function<T, List<T>> getChildren) {
        List<T> matches = doBreadthFirstSearch(list, isMatching, getChildren, true);
        if (matches.size() > 0)
            return matches.get(0);
        else
            return null;
    }

    /**
     * Performs a breadth first search, stopping when the entire list (tree) is traversed.
     *
     * @param list List to search
     * @param isMatching A predicate to invoke to determine whether an item in the list matches
     *                   some criteria
     * @param getChildren A function to get the children of an item in the list, if there are any.
     *                    If there are no children, the function should return an empty list.
     * @param <T> The type of item to search for
     * @return All matching results
     */
    public static <T> List<T> breadthFirstSearchMany(List<T> list,
                                        Predicate<T> isMatching,
                                        Function<T, List<T>> getChildren) {
        return doBreadthFirstSearch(list, isMatching, getChildren, false);
    }

    /**
     * Performs a breadth first search
     *
     * @param list The list of root entries in the tree
     * @param isMatching A {@link Predicate} to determine whether an entry matches some criteria
     *                   (if so, it will be added to the list of matches and returned)
     * @param getChildren A {@link Function} that takes an entry and returns a list of children of
     *                    that entry, so that they can be traversed.
     * @param stopAfterOne Whether to stop after finding a single matching result. In this case the
     *                     returned list will have a size of 1.
     * @param <T> The type representing an entry/node in the tree
     * @return A list of matching entries/nodes
     */
    private static <T> List<T> doBreadthFirstSearch(List<T> list,
                                             Predicate<T> isMatching,
                                             Function<T, List<T>> getChildren,
                                             boolean stopAfterOne) {
        List<T> matches = new ArrayList<>();
        Deque<T> unexplored = new ArrayDeque<>(list);

        while (!unexplored.isEmpty()) {
            T node = unexplored.removeFirst();

            // Check if this node is matching
            if (isMatching.test(node)) {
                matches.add(node);
                if (stopAfterOne)
                    return matches;
            }

            // Queue the children of this node, if it has any
            unexplored.addAll(getChildren.apply(node));
        }

        return matches;
    }

}
