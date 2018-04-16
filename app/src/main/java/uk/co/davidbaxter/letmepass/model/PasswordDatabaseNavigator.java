package uk.co.davidbaxter.letmepass.model;

import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class PasswordDatabaseNavigator {

    private final PasswordDatabase database;
    private final Deque<FolderEntry> hierarchyStack = new ArrayDeque<>();

    public PasswordDatabaseNavigator(PasswordDatabase database) {
        this.database = database;
    }

    public boolean isAtRoot() {
        return this.hierarchyStack.isEmpty();
    }

    public @Nullable FolderEntry getFolder() {
        return this.hierarchyStack.peekLast();
    }

    public String getFolderName() {
        return this.hierarchyStack.isEmpty() ? "" : this.hierarchyStack.peekLast().name;
    }

    public List<PasswordDatabaseEntry> getFolderEntries() {
        return this.hierarchyStack.isEmpty() ? this.database.getRootEntries()
                : this.hierarchyStack.peekLast().children;
    }

    public void openFolder(FolderEntry folder) {
        this.hierarchyStack.addLast(folder);
    }

    public void closeFolder() {
        this.hierarchyStack.removeLast();
    }

}
