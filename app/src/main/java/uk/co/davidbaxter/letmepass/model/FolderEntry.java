package uk.co.davidbaxter.letmepass.model;

public class FolderEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "folder";

    public FolderEntry(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public int getEntryCount() {
        return 4; // TODO
    }

    // TODO: impl additional fields

}
