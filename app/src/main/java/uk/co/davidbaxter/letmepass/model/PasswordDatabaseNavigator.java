package uk.co.davidbaxter.letmepass.model;

import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * A class to navigate hierarchical {@link PasswordDatabase} objects
 */
public class PasswordDatabaseNavigator {

    private final PasswordDatabase database;
    private final Deque<FolderEntry> hierarchyStack = new ArrayDeque<>();

    /**
     * Constructs a navigator for the given database
     * @param database Database to navigate
     */
    public PasswordDatabaseNavigator(PasswordDatabase database) {
        this.database = database;
    }

    /**
     * Checks whether the navigator is positioned at the root of the folder hierarchy
     * @return True if navigator at root
     */
    public boolean isAtRoot() {
        return this.hierarchyStack.isEmpty();
    }

    /**
     * Gets the current folder (if not in root)
     * @return The current folder, or null if in root
     */
    public @Nullable FolderEntry getFolder() {
        return this.hierarchyStack.peekLast();
    }

    /**
     * Gets the name of the current folder
     * @return Name of current folder, or empty string if in root
     */
    public String getFolderName() {
        return this.hierarchyStack.isEmpty() ? "" : this.hierarchyStack.peekLast().name;
    }

    /**
     * Gets a list of entries within the current folder
     * @return Entries in the current folder (or in the root if in root)
     */
    public List<PasswordDatabaseEntry> getFolderEntries() {
        return this.hierarchyStack.isEmpty() ? this.database.getRootEntries()
                : this.hierarchyStack.peekLast().children;
    }

    /**
     * Navigates to a folder
     * @param folder Folder to navigate to
     */
    public void openFolder(FolderEntry folder) {
        this.hierarchyStack.addLast(folder);
    }

    /**
     * Closes the last opened folder
     */
    public void closeFolder() {
        this.hierarchyStack.removeLast();
    }

}
