package uk.co.davidbaxter.letmepass.model;

import java.util.List;

/**
 * A database that stores password database entries (see {@link PasswordDatabaseEntry}).
 * <p>
 * This database class provides means of serialization, so that it can be stored.
 */
public interface PasswordDatabase {

    /**
     * Adds an entry to the database in the root
     * @param entry Entry to add
     */
    void addEntry(PasswordDatabaseEntry entry);

    /**
     * Adds an entry to a folder (or another entry, depending on implementation) in the database
     * @param entry Entry to add
     * @param parent Folder to add the entry to
     */
    void addEntry(PasswordDatabaseEntry entry, PasswordDatabaseEntry parent);

    /**
     * Deletes an entry from the database (at any level: it does not have to be a root entry).
     *
     * @param entry Entry to delete
     * @return Whether the entry was found & deleted
     */
    boolean deleteEntry(PasswordDatabaseEntry entry);

    /**
     * Gets the entries in the root of this database
     * @return Root entries
     */
    List<PasswordDatabaseEntry> getRootEntries();

    /**
     * Gets the entries marked as favorite in the database
     * @see PasswordDatabaseEntry
     * @return List of entries marked as favorite
     */
    List<PasswordDatabaseEntry> getFavorites();

    /**
     * Gets all entries in the database, regardless of hierarchical level. Note that this method
     * will return folders in addition to other entries.
     *
     * @return All entries in the database
     */
    List<PasswordDatabaseEntry> getAllEntries();

    /**
     * Gets all entries that are <b>not folders</b> in the database.
     * @return All entries in the database that are not folder entries
     */
    List<PasswordDatabaseEntry> getAllEntriesButFolders();

    /**
     * Searches the database for an entry that matches a keyword based on some criteria. This
     * criteria is implementation-specific.
     *
     * @param keyword Keyword to search for
     * @return List of matching entries
     */
    List<PasswordDatabaseEntry> search(String keyword);

    /**
     * Gets the name of this database
     * @return Name of database
     */
    String getName();

    /**
     * Sets the name of this database
     * @param name New name of database
     */
    void setName(String name);

    /**
     * Serializes the database into a byte format suitable for storage
     * @return Database at time of call in serialized byte format
     */
    byte[] serialize();

}
