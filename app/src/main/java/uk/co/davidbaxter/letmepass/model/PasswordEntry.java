package uk.co.davidbaxter.letmepass.model;

import java.util.Date;

public class PasswordEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "password";

    public final String username;

    public PasswordEntry(String name, String username) {
        super(name);
        this.username = username;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    // TODO: impl additional fields

}
