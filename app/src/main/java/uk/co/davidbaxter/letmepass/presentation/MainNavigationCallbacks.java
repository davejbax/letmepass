package uk.co.davidbaxter.letmepass.presentation;

import android.util.Pair;

import uk.co.davidbaxter.letmepass.R;

/**
 * Callbacks for navigation related actions for the MainViewModel
 */
public class MainNavigationCallbacks {

    private final MainViewModel viewModel;

    public MainNavigationCallbacks(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Handles a change in display mode (i.e. which entries are shown) selection, updating entries,
     * the title, and state.
     *
     * @param mode The selected mode
     */
    public void onModeSelected(DisplayMode mode) {
        // TODO: note in practice entries.postValue(model.getEntries()) etc. may be replaced by a
        // callback so as to not slow the UI
        switch (mode) {
            case EXPLORE:
                viewModel.screenTitle.postValue(new Pair<Integer, Object[]>(
                        R.string.main_title_explore,
                        null // No format params
                ));
                viewModel.entries.postValue(viewModel.model.getEntries());
                break;

            case FAVORITES:
                viewModel.screenTitle.postValue(new Pair<Integer, Object[]>(
                        R.string.main_title_favorites,
                        null // No format params
                ));
                viewModel.entries.postValue(viewModel.model.getFavorites());
                break;

            case ALL_PASSWORDS:
                viewModel.screenTitle.postValue(new Pair<Integer, Object[]>(
                        R.string.main_title_all_passwords,
                        null // No format params
                ));
                viewModel.entries.postValue(viewModel.model.getAllPasswords());
                break;
        }

        viewModel.currentMode = mode;
    }

    /**
     * Handles an action of opening the configuration screen for a database
     */
    public void onConfigure() {
        // TODO
    }

    /**
     * Handles an action of closing the database
     */
    public void onCloseDatabase() {
        // TODO
    }

}
