package uk.co.davidbaxter.letmepass.util;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DebounceUtils {

    /**
     * Threaded scheduler service to run search debounce tasks in a scheduled fashion.
     */
    private static final ScheduledExecutorService executorService
            = Executors.newScheduledThreadPool(3);

    /**
     * TODO
     */
    private static Map<Pair<Object, String>, ScheduledFuture<?>> timers = new HashMap<>();

    public static void debounce(Object owner, String id, int delayMs, Runnable runnable) {
        Pair key = Pair.create(owner, id);
        if (timers.containsKey(key))
            timers.get(key).cancel(false);

        timers.put(key, executorService.schedule(runnable, delayMs, TimeUnit.MILLISECONDS));
    }

    public static void cancel(Object owner, String id) {
        Pair key = Pair.create(owner, id);
        if (timers.containsKey(key))
            timers.get(key).cancel(false);

        timers.remove(key);
    }

}
