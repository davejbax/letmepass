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

    public static int levUnlimited(String left, String right) {
        return LEV_UNLIMITED.apply(left, right);
    }

    public static int levLimited(String left, String right, boolean lowLimit) {
        if (lowLimit)
            return LEV_LIMITED_LOW.apply(left, right);
        else
            return LEV_LIMITED_HIGH.apply(left, right);
    }

    public static <T> T breadthFirstSearch(List<T> list,
                                    Predicate<T> isMatching,
                                    Function<T, List<T>> getChildren) {
        List<T> matches = doBreadthFirstSearch(list, isMatching, getChildren, true);
        if (matches.size() > 0)
            return matches.get(0);
        else
            return null;
    }

    public static <T> List<T> breadthFirstSearchMany(List<T> list,
                                        Predicate<T> isMatching,
                                        Function<T, List<T>> getChildren) {
        return doBreadthFirstSearch(list, isMatching, getChildren, false);
    }

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
