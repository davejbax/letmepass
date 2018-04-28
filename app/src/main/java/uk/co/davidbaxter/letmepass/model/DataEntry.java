package uk.co.davidbaxter.letmepass.model;

/** A data entry in a password database */
public class DataEntry extends PasswordDatabaseEntry {

    public static final String TYPE = "data";

    public String data;

    public DataEntry(String name, String data) {
        super(name);
        this.data = data;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object clone() {
        return new DataEntry(name, data);
    }

    @Override
    public void fromCopy(PasswordDatabaseEntry entry) {
        if (!(entry instanceof DataEntry))
            throw new IllegalArgumentException("Entry is not of correct type (data)");

        super.fromCopy(entry);
        DataEntry dEntry = (DataEntry) entry;
        this.data = dEntry.data;
    }

    public static DataEntry newEmptyEntry() {
        return new DataEntry("New data", "Enter any text here!");
    }

}
