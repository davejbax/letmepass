package uk.co.davidbaxter.letmepass.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A folder entry in a password database, storing child entries */
public class FolderEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "folder";

    public List<PasswordDatabaseEntry> children = new ArrayList<>();

    public FolderEntry(String name, List<PasswordDatabaseEntry> children) {
        super(name);
        this.children.addAll(children);
    }

    public FolderEntry(String name) {
        this(name, Collections.<PasswordDatabaseEntry>emptyList());
    }

    public int getEntryCount() {
        return children.size();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object clone() {
        List<PasswordDatabaseEntry> clonedChildren = new ArrayList<>();
        for (PasswordDatabaseEntry entry : children)
            clonedChildren.add((PasswordDatabaseEntry) entry.clone());
        return new FolderEntry(name, clonedChildren);
    }

    @Override
    public void fromCopy(PasswordDatabaseEntry entry) {
        if (!(entry instanceof FolderEntry))
            throw new IllegalArgumentException("Entry is not of correct type (folder)");

        super.fromCopy(entry);
        FolderEntry fEntry = (FolderEntry) entry;
        this.children.clear();
        this.children.addAll(fEntry.children);
    }

    public static FolderEntry newEmptyEntry() {
        return new FolderEntry("New folder");
    }

    // TODO: impl additional fields

}
