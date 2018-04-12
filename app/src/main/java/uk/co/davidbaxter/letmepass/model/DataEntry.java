package uk.co.davidbaxter.letmepass.model;

public class DataEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "data";

    public DataEntry(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    // TODO: impl additional fields

}
