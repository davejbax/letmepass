package uk.co.davidbaxter.letmepass.presentation;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.util.Triplet;

/**
 * Callbacks of the MainViewModel relating to entries in the view
 */
public class MainEntryCallbacks {

    private final MainViewModel viewModel;

    public MainEntryCallbacks(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Called when a container is clicked in the view
     * @param container Container that was clicked
     */
    public void onContainerClick(@NonNull PasswordDatabaseEntryContainer container) {
        if (container.isDivider())
            return;

        // TODO: handle entry click
    }

    /**
     * Called when an entry's popup menu should be opened (i.e. when overflow button pressed)
     * @param v View to anchor menu to
     * @param container Container of entry
     */
    public void onEntryMenuOpen(View v, PasswordDatabaseEntryContainer container) {
        this.viewModel.popupMenu.postValue(new Triplet<View, Integer, Object>(
                v, R.menu.menu_main_entry_popup, container));
    }

    /**
     * Called when a user chooses to toggle the favorite status of an entry
     * @param container Container of entry
     */
    public void onToggleFavorite(PasswordDatabaseEntryContainer container) {
        // TODO
    }

    /**
     * Called when a user chooses to edit an entry
     * @param container Container of entry
     */
    public void onEditEntry(PasswordDatabaseEntryContainer container) {
        // TODO
    }

    /**
     * Called when a user chooses to delete an entry
     * @param container Container of entry
     */
    public void onDeleteEntry(PasswordDatabaseEntryContainer container) {
        // TODO
    }

    public void onNewFolder() {
        // TODO
    }

    public void onNewData() {
        // TODO
    }

    public void onNewPassword() {
        // TODO
    }

}
