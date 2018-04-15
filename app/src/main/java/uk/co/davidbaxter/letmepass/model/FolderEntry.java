package uk.co.davidbaxter.letmepass.model;

public class FolderEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "folder";

    public FolderEntry(String name) {
        super(name);
    }

    public int getEntryCount() {
        return 4; // TODO
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object clone() {
        return new FolderEntry(name);
    }

    @Override
    public void fromCopy(PasswordDatabaseEntry entry) {
        if (!(entry instanceof FolderEntry))
            throw new IllegalArgumentException("Entry is not of correct type (folder)");

        super.fromCopy(entry);
        FolderEntry fEntry = (FolderEntry) entry;
        // TODO here: copy impl-specific fields
    }

    public static FolderEntry newEmptyEntry() {
        return new FolderEntry("New folder");
    }

    // TODO: impl additional fields

}
