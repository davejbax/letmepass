package uk.co.davidbaxter.letmepass.model;

import java.util.List;

public interface PasswordDatabase {

    void addEntry(PasswordDatabaseEntry entry);

    void addEntry(PasswordDatabaseEntry entry, PasswordDatabaseEntry parent);

    boolean deleteEntry(PasswordDatabaseEntry entry);

    List<PasswordDatabaseEntry> getRootEntries();

    List<PasswordDatabaseEntry> getFavorites();

    List<PasswordDatabaseEntry> getAllEntries();

    List<PasswordDatabaseEntry> search(String keyword);

    String getName();

    void setName(String name);

    byte[] serialize();

}
