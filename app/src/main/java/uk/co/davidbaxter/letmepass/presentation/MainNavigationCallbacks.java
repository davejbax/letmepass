package uk.co.davidbaxter.letmepass.presentation;

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
        viewModel.currentMode = mode;
        viewModel.refreshView();
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
