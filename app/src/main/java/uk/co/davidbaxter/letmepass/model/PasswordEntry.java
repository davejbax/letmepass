package uk.co.davidbaxter.letmepass.model;

public class PasswordEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "password";

    public String username;
    public String password;
    public String website;
    public String description;

    public PasswordEntry(String name, String username, String password, String website,
                         String description) {
        super(name);
        this.username = username;
        this.password = password;
        this.website = website;
        this.description = description;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object clone() {
        return new PasswordEntry(name, username, password, website, description);
    }

    @Override
    public void fromCopy(PasswordDatabaseEntry entry) {
        if (!(entry instanceof PasswordEntry))
            throw new IllegalArgumentException("Entry is not of correct type (password)");

        super.fromCopy(entry);
        PasswordEntry pEntry = (PasswordEntry) entry;
        this.username = pEntry.username;
        this.password = pEntry.password;
        this.website = pEntry.website;
        this.description = pEntry.description;
    }

    public static PasswordEntry newEmptyEntry() {
        return new PasswordEntry("New password", "", "TODO", "", "");
    }

    // TODO: impl additional fields

}
