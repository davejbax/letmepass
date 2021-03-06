package uk.co.davidbaxter.letmepass.presentation;

import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.security.SecurityServices;
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

        // If opening a folder, navigate to it in navigator and refresh title/entries
        if (container.getEntry().getType().equals(FolderEntry.TYPE)) {
            this.viewModel.getNavigationCallbacks().onOpenFolder(container);
        // If any other entry, open dialog to view it
        } else {
            this.viewModel.dialog.postValue(new Pair<PasswordDatabaseEntryContainer, Boolean>(
                    container,
                    false // Not editable
            ));
        }

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
        container.getEntry().favorite = !container.getEntry().favorite;
        this.saveEntry(container);
    }

    /**
     * Called when a user chooses to edit an entry
     * @param container Container of entry
     */
    public void onEditEntry(PasswordDatabaseEntryContainer container) {
        this.viewModel.dialog.postValue(new Pair<PasswordDatabaseEntryContainer, Boolean>(
                container, true // true = editable
        ));
    }

    /**
     * Called when a user chooses to delete an entry
     * @param container Container of entry
     */
    public void onDeleteEntry(PasswordDatabaseEntryContainer container) {
        this.viewModel.database.deleteEntry(container.getEntry());
        this.viewModel.refreshView();
        this.viewModel.saveDatabase();
    }

    public void onNewFolder() {
        this.viewModel.dialog.postValue(new Pair<PasswordDatabaseEntryContainer, Boolean>(
                new PasswordDatabaseEntryContainer(FolderEntry.newEmptyEntry(), null),
                true // Editable
        ));
    }

    public void onNewData() {
        this.viewModel.dialog.postValue(new Pair<PasswordDatabaseEntryContainer, Boolean>(
                new PasswordDatabaseEntryContainer(DataEntry.newEmptyEntry(), null),
                true // Editable
        ));
    }

    public void onNewPassword() {
        String generatedPwd = SecurityServices
                .getInstance()
                .getPasswordGeneratorService()
                .getPasswordGenerator()
                .generate();
        this.viewModel.dialog.postValue(new Pair<PasswordDatabaseEntryContainer, Boolean>(
                new PasswordDatabaseEntryContainer(
                        new PasswordEntry(
                            "New password", "", generatedPwd, "", ""
                        ),
                        null),
                true // Editable
        ));
    }

    public boolean onCopyPassword(PasswordDatabaseEntryContainer container) {
        if (!container.getEntry().getType().equals(PasswordEntry.TYPE))
            return false;

        PasswordEntry entry = (PasswordEntry) container.getEntry();
        this.viewModel.copyToClipboard.postValue(entry.password);
        return true;
    }

    /**
     * Saves an entry held in a container to the model
     * @param container Container containing entry to save
     */
    public void saveEntry(PasswordDatabaseEntryContainer container) {
        if (this.viewModel.navigator.isAtRoot()) {
            this.viewModel.database.addEntry(container.getEntry());
        } else {
            this.viewModel.database.addEntry(
                    container.getEntry(),
                    this.viewModel.navigator.getFolder()
            );
        }
        // If we are saving an existing entry, then simply update the container
        if (this.viewModel.getContainers().getValue().contains(container)) {
            this.viewModel.updateContainer.postValue(container);
        // If we have a new entry (not in our list), refresh view/entries to get it and update view
        } else {
            // Refresh the view (i.e. re-get and set all entries)
            // TODO: fix bug here: if searching and then adding a new entry, search results will be replaced
            this.viewModel.refreshView();
        }

        // Save the DB
        this.viewModel.saveDatabase();
    }

}
