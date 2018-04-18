package uk.co.davidbaxter.letmepass.presentation;

import android.util.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import uk.co.davidbaxter.letmepass.R;

/**
 * Callbacks for the search functionality of the main view, as part of the MainViewModel
 */
public class MainSearchCallbacks {

    /**
     * How long in ms to wait after the user types a character to execute the search query. If they
     * type another character in this time, the timer resets.
     */
    private static final int SEARCH_DEBOUNCE_TIME = 400;

    /**
     * Threaded scheduler service to run search debounce tasks in a scheduled fashion.
     */
    private final ScheduledExecutorService searchExecutor = Executors.newScheduledThreadPool(1);

    /**
     * Current search debounce task; may be cancelled, and may be null.
     */
    private ScheduledFuture<?> searchDebounce = null;

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
        if (this.searchDebounce != null)
            this.searchDebounce.cancel(true);

        // Retrieve search results from the model
        // TODO: in practice, use a Callback to post this value: i.e. model takes a callback arg
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

        // Forcefully cancel currently scheduled debounce timer
        if (this.searchDebounce != null)
            this.searchDebounce.cancel(true);

        // Schedule a new debounce task to carry out the query after the user is 'done' typing
        this.searchDebounce = this.searchExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                onSearchSubmit(query);
            }
        }, SEARCH_DEBOUNCE_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * Handles a close of the search view entirely
     */
    public void onSearchClose() {
        // Cancel any debounce task as we are done searching
        if (this.searchDebounce != null)
            this.searchDebounce.cancel(true);

        // Update screen title to default and display all entries again (mimic screen selected call)
        viewModel.getNavigationCallbacks().onModeSelected(viewModel.currentMode);
    }

}
