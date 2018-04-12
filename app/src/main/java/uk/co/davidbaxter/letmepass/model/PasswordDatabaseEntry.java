package uk.co.davidbaxter.letmepass.model;

import java.util.Date;

// TODO: doc; it is immutable bc it would break things otherwise
public abstract class PasswordDatabaseEntry {

    public final String name;
    public final long created;
    public final long updated;
    public final boolean favorite;

    protected PasswordDatabaseEntry(String name, long created, long updated, boolean favorite) {
        this.name = name;
        this.created = created;
        this.updated = updated;
        this.favorite = favorite;
    }

    protected PasswordDatabaseEntry(String name) {
        this.name = name;
        this.created = new Date().getTime();
        this.updated = created;
        this.favorite = false;
    }

    /**
     * Gets the type of this entry as a unifying String. This may be used for purposes of
     * serialization or comparison.
     *
     * @return Type of entry
     */
    public abstract String getType();

}
