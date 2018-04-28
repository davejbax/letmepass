package uk.co.davidbaxter.letmepass.util;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to perform 'debounce' functionality
 * <p>
 * Debouncing a call is to start a timer that, when expires, invokes the call. Each call to the
 * debounce function will restart this timer, if the timer has not yet expired. This can be useful
 * in limiting the calls to an expensive function to only occur after all data has been collected,
 * for instance, in spite of a stream-like data collection.
 */
public class DebounceUtils {

    /** Threaded scheduler service to run search debounce tasks in a scheduled fashion */
    private static final ScheduledExecutorService executorService
            = Executors.newScheduledThreadPool(3);

    /** Map of identifiers (as a pair of owner and key) to timers/futures */
    private static Map<Pair<Object, String>, ScheduledFuture<?>> timers = new HashMap<>();

    /**
     * Debounces a function
     * @param owner Owner of the function
     * @param id An ID to represent this particular function to debouncee
     * @param delayMs The number of milliseconds to delay before executing the function, provided
     *                that the timer is not restarted.
     * @param runnable The function to call
     */
    public static void debounce(Object owner, String id, int delayMs, Runnable runnable) {
        Pair key = Pair.create(owner, id);
        if (timers.containsKey(key))
            timers.get(key).cancel(false);

        timers.put(key, executorService.schedule(runnable, delayMs, TimeUnit.MILLISECONDS));
    }

    /**
     * Cancels a debounce timer
     * @param owner Owner of the function that was being debounced
     * @param id ID of the function that was being debounced
     */
    public static void cancel(Object owner, String id) {
        Pair key = Pair.create(owner, id);
        if (timers.containsKey(key))
            timers.get(key).cancel(false);

        timers.remove(key);
    }

}
