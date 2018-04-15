package uk.co.davidbaxter.letmepass.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for password database entries. This class must be serializable and cloneable:
 * serializable for the purposes of serializing/saving, cloneable because this class is inherently
 * mutable as this makes editing entries easier.
 * <p>
 * Subclasses should override {@link #fromCopy(PasswordDatabaseEntry)}.
 */
public abstract class PasswordDatabaseEntry implements Serializable, Cloneable {

    public String name;
    public long created;
    public long updated;
    public boolean favorite;

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

    /**
     * Sets the fields of this entry to those of another entry. This is so contents can be changed
     * while preserving a reference.
     * <p>
     * Subclasses should override this method to set subclass-specific fields.
     *
     * @param entry Entry from which to copy fields
     */
    public void fromCopy(PasswordDatabaseEntry entry) {
        this.name = entry.name;
        this.created = entry.created;
        this.updated = entry.updated;
        this.favorite = entry.favorite;
    }

    @Override
    public abstract Object clone();
}
