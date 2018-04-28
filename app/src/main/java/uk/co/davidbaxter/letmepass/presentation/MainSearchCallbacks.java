package uk.co.davidbaxter.letmepass.presentation;

import android.util.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.util.DebounceUtils;

/**
 * Callbacks for the search functionality of the main view, as part of the MainViewModel
 */
public class MainSearchCallbacks {
//
//    /**
//     * How long in ms to wait after the user types a character to execute the search query. If they
//     * type another character in this time, the timer resets.
//     */
//    private static final int SEARCH_DEBOUNCE_TIME = 400;
//
//    /**
//     * Threaded scheduler service to run search debounce tasks in a scheduled fashion.
//     */
//    private final ScheduledExecutorService searchExecutor = Executors.newScheduledThreadPool(1);
//
//    /**
//     * Current search debounce task; may be cancelled, and may be null.
//     */
//    private ScheduledFuture<?> searchDebounce = null;
    private static final String DEBOUNCE_KEY_SEARCH = "search";

    private static final int DEBOUNCE_TIME_MS = 400;

    private final MainViewModel viewModel;

    public MainSearchCallbacks(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Submits a search query to the model, and updates the relevant LiveData when done
     *
     * @param query Query to submit
     */
    public void onSearchSubmit(String query) {
        // If query is blank, return to the previous state
        if (query.equals("")) {
            this.onSearchClose(); // Pretend we closed search
            return;
        }

        // Cancel any debounce timers as we are done typing
        DebounceUtils.cancel(this, DEBOUNCE_KEY_SEARCH);

        // Retrieve search results from the model
        viewModel.entries.postValue(viewModel.database.search(query));

        // Update title and recreate containers to update view
        viewModel.screenTitle.postValue(new Pair<Integer, Object[]>(
                R.string.main_title_search_results,
                new Object[]{ query }
        ));
    }

    /**
     * Handles a change in search query and interacts with the model appropriately
     *
     * @param query New query
     */
    public void onSearchChange(final String query) {
        // If we have a blank query, we probably removed our text. Don't do debounce in this case.
        if (query.equals("")) {
            this.onSearchClose();
            return;
        }

        // Schedule a new debounce task to carry out the query after the user is 'done' typing
        // (this cancels any existing debounce and runs asynchronously).
        DebounceUtils.debounce(this, DEBOUNCE_KEY_SEARCH, DEBOUNCE_TIME_MS, new Runnable() {
            @Override
            public void run() {
                onSearchSubmit(query);
            }
        });
    }

    /**
     * Handles a close of the search view entirely
     */
    public void onSearchClose() {
        // Cancel any debounce task as we are done searching
        DebounceUtils.cancel(this, DEBOUNCE_KEY_SEARCH);

        // Update screen title to default and display all entries again (mimic screen selected call)
        viewModel.getNavigationCallbacks().onModeSelected(viewModel.currentMode);
    }

}
