package uk.co.davidbaxter.letmepass;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.model.impl.JsonPasswordDatabase;

public class JsonPasswordDatabaseTest {

    @Test
    public void serializes_EmptyDatabase() throws IOException {
        // Create empty DB
        JsonPasswordDatabase db = new JsonPasswordDatabase("test",
                Collections.<PasswordDatabaseEntry>emptyList());

        // Attempt to serialize & deserialize empty DB
        byte[] serialized = db.serialize();
        JsonPasswordDatabase newDb = JsonPasswordDatabase.deserialize(serialized, false);

        // Assert deserialized DB is equal to original
        assertThat(newDb.getName(), is("test"));
        assertThat(newDb.getRootEntries().isEmpty(), is(true));
    }

    @Test
    public void serializes_AllEntryTypes() throws IOException {
        // Create a list of entries
        String dbName = "test";
        PasswordEntry passEntry = new PasswordEntry("test", "user", "pass", "website", "desc");
        FolderEntry folderEntry = new FolderEntry("folder");
        DataEntry dataEntry = new DataEntry("data test", "my data here");
        List<PasswordDatabaseEntry> entries = Arrays.asList(
                passEntry, folderEntry, dataEntry
        );

        // Create the DB to serialize
        JsonPasswordDatabase db = new JsonPasswordDatabase(dbName, entries);

        // Serialize and deserialize the DB
        byte[] serialized = db.serialize();
        JsonPasswordDatabase newDb = JsonPasswordDatabase.deserialize(serialized, false);

        // Ensure that the DB deserialized correctly
        assertThat(newDb.getName(), is(dbName));
        assertThat(newDb.getRootEntries().size(), is(entries.size()));
        for (PasswordDatabaseEntry entry : newDb.getRootEntries()) {
            assertThat(entry.name, anyOf(
                    is(passEntry.name),
                    is(folderEntry.name),
                    is(dataEntry.name)
            ));
        }
    }

    @Test
    public void filters_AllEntriesAndRoot() {
        // Create entries
        PasswordDatabaseEntry entry1 = new PasswordEntry("Password", "Test", "test", "", "");
        PasswordDatabaseEntry entry2 = new PasswordEntry("Nested password 1", "", "", "", "");
        PasswordDatabaseEntry entry3 = new PasswordEntry("Nested password 2", "", "", "", "");

        // Create DB with two entries in the root (one pwd, one folder), and two in the folder
        PasswordDatabase db = new JsonPasswordDatabase("db", Arrays.asList(
                entry1,
                new FolderEntry("Folder", Arrays.<PasswordDatabaseEntry>asList(
                        entry2,
                        entry3
                ))
        ));

        assertThat(db.getAllEntries(), hasItems(entry1, entry2, entry3)); // All entries has all
        assertThat(db.getRootEntries(), hasItems(entry1)); // Root entries should have 1
        assertThat(db.getRootEntries().size(), is(2)); // Folder and password entry in root
    }

    @Test
    public void filters_Favorites() {
        // Create one favorite and one non-favorite entry
        PasswordDatabaseEntry notFavorite = new PasswordEntry("one", "", "", "", "");
        PasswordDatabaseEntry favorite = new PasswordEntry("two" ,"", "", "", "");
        favorite.favorite = true;

        // Create DB and ensure favorite filtered correctly
        PasswordDatabase db = new JsonPasswordDatabase("db", Arrays.asList(notFavorite, favorite));
        assertThat(db.getFavorites().size(), is(1));
        assertThat(db.getFavorites(), hasItems(favorite));
    }

    @Test
    public void deletes_NestedEntry() {
        // Create entries to be nested at different levels (1 at first, 2 at second, etc.)
        PasswordDatabaseEntry entry1 = new PasswordEntry("Password", "Test", "test", "", "");
        PasswordDatabaseEntry entry2 = new PasswordEntry("Nested password 1", "", "", "", "");
        PasswordDatabaseEntry entry3 = new PasswordEntry("Nested password 2", "", "", "", "");

        PasswordDatabase db = new JsonPasswordDatabase("db", Arrays.asList(
                entry1,
                new FolderEntry("Folder", Arrays.asList(
                        entry2,
                        new FolderEntry("Folder 2", Arrays.asList(
                                entry3
                        ))
                ))
        ));

        // Try deleting entries and ensuring they are deleted correctly
        assertThat(db.deleteEntry(entry2), is(true)); // Should delete 2 successfully
        assertThat(db.deleteEntry(entry3), is(true)); // Should delete 3 successfully
        assertThat(db.deleteEntry(entry3), is(false)); // Should not delete same one twice
        assertThat(db.getAllEntriesButFolders().size(), is(1)); // One pwd entry left now
    }

    @Test
    public void searches_RepeatedDataMixedCase() {
        PasswordDatabase db = new JsonPasswordDatabase("db",
                Collections.<PasswordDatabaseEntry>emptyList());

        // Matching
        db.addEntry(new PasswordEntry("one", "", "", "", ""));
        db.addEntry(new PasswordEntry("ONE", "", "", "", ""));
        db.addEntry(new PasswordEntry("onetwo", "", "", "", ""));
        db.addEntry(new PasswordEntry("fourONE", "", "", "", ""));

        // Non-matching
        db.addEntry(new PasswordEntry("two", "", "", "", ""));
        db.addEntry(new PasswordEntry("three", "", "", "", ""));
        db.addEntry(new PasswordEntry("four", "", "", "", ""));

        List<PasswordDatabaseEntry> results = db.search("one");
        assertThat(results.size() ,is(4));
    }

    @Test
    public void searches_NonMatching() {
        PasswordDatabase db = new JsonPasswordDatabase("db",
                Collections.<PasswordDatabaseEntry>emptyList());
        db.addEntry(new PasswordEntry("one", "", "", "", ""));
        db.addEntry(new PasswordEntry("two", "", "", "", ""));

        // Get search results
        List<PasswordDatabaseEntry> results = db.search("three");
        assertThat(results.size(), is(0));
    }

}
