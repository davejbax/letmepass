package uk.co.davidbaxter.letmepass.presentation;

import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;

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
        this.viewModel.closeEvent.postValue(null);
    }

    public void onOpenFolder(PasswordDatabaseEntryContainer container) {
        if (container.getEntry().getType().equals(FolderEntry.TYPE)) {
            this.viewModel.navigator.openFolder((FolderEntry) container.getEntry());
            this.viewModel.canGoBack.postValue(true);
            this.viewModel.refreshView();
        }
    }

    public void onGoBack() {
        this.viewModel.navigator.closeFolder();
        this.viewModel.canGoBack.postValue(!this.viewModel.navigator.isAtRoot());
        this.viewModel.refreshView();
    }

}
