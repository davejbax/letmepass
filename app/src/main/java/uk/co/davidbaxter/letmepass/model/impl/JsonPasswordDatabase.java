package uk.co.davidbaxter.letmepass.model.impl;

import android.arch.core.util.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.util.Algorithms;
import uk.co.davidbaxter.letmepass.util.Predicate;

/**
 * An implementation of {@link PasswordDatabase} that uses JSON to serialize/deserialize the
 * database.
 */
public class JsonPasswordDatabase implements PasswordDatabase {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new PasswordDatabaseEntryTypeAdapterFactory())
            .create();
    private static final String CHARSET_NAME = "UTF-8";
    private static final Function<PasswordDatabaseEntry, List<PasswordDatabaseEntry>> GET_CHILDREN
            = new Function<PasswordDatabaseEntry, List<PasswordDatabaseEntry>>() {
        @Override
        public List<PasswordDatabaseEntry> apply(PasswordDatabaseEntry entry) {
            // Return children if folder, or empty list otherwise
            return (entry instanceof FolderEntry) ? ((FolderEntry) entry).children
                    : Collections.<PasswordDatabaseEntry>emptyList();
        }
    };

    private List<PasswordDatabaseEntry> entries = new ArrayList<>();
    private String name = "";

    // Default constructor for Gson reflection
    private JsonPasswordDatabase() {}

    /**
     * Constructs a new JsonPasswordDatabase with the given name and entries.
     *
     * @param name Name of database
     * @param entries Entries to add to the database (may be empty)
     */
    public JsonPasswordDatabase(String name, Collection<PasswordDatabaseEntry> entries) {
        this.name = name;
        this.entries.addAll(entries);
    }

    @Override
    public void addEntry(PasswordDatabaseEntry entry) {
        this.entries.add(entry);
    }

    @Override
    public void addEntry(PasswordDatabaseEntry entry, PasswordDatabaseEntry parent) {
        if (!(parent instanceof FolderEntry))
            return;

        FolderEntry folder = (FolderEntry) parent;
        folder.children.add(entry);
    }

    @Override
    public boolean deleteEntry(PasswordDatabaseEntry entry) {
        if (entries.contains(entry))
            return entries.remove(entry);

        PasswordDatabaseEntry parent = findParent(entry);
        if (parent == null)
            return false;

        FolderEntry folder = (FolderEntry) parent;
        return folder.children.remove(entry);
    }

    @Override
    public List<PasswordDatabaseEntry> getRootEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public List<PasswordDatabaseEntry> getFavorites() {
        ArrayList<PasswordDatabaseEntry> favorites = new ArrayList<>();
        for (PasswordDatabaseEntry entry : entries)
            if (entry.favorite)
                favorites.add(entry);

        return favorites;
    }

    @Override
    public List<PasswordDatabaseEntry> getAllEntries() {
        return Algorithms.breadthFirstSearchMany(
            this.entries,
            new Predicate<PasswordDatabaseEntry>() {
                @Override
                public boolean test(PasswordDatabaseEntry entry) {
                    // Return all entries
                    return true;
                }
            },
            GET_CHILDREN
        );
    }

    @Override
    public List<PasswordDatabaseEntry> getAllEntriesButFolders() {
        List<PasswordDatabaseEntry> entries = new ArrayList<>(getAllEntries());
        Iterator<PasswordDatabaseEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            // Remove entries that are folders from the list
            if (iter.next().getType().equals(FolderEntry.TYPE))
                iter.remove();
        }

        return entries;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<PasswordDatabaseEntry> search(String keyword) {
        // Convert keyword to lowercase; we compare lowercase only to increase matches without
        // sacrificing practicality (users likely do not care about case)
        keyword = keyword.toLowerCase();

        // Retrieve all database entries, and construct a map of matching entries to their Lev dist
        List<PasswordDatabaseEntry> allEntries = getAllEntries();
        final Map<PasswordDatabaseEntry, Integer> matches = new HashMap<>();

        // Add the entries that have a name containing the keyword to the matches, calculating their
        // Levenshtein distance
        for (PasswordDatabaseEntry entry : allEntries) {
            if (entry.name.toLowerCase().contains(keyword)) {
                int levDistance = Algorithms.levLimited(entry.name, keyword, false);
                matches.put(entry, levDistance == -1 ? Integer.MAX_VALUE : levDistance);
            }
        }

        // Form a list of matches and order them by their Levenshtein distance (ascending)
        List<PasswordDatabaseEntry> orderedMatches = new ArrayList<>(matches.keySet());
        Collections.sort(orderedMatches, new Comparator<PasswordDatabaseEntry>() {
            @Override
            public int compare(PasswordDatabaseEntry o1, PasswordDatabaseEntry o2) {
                return matches.get(o1).compareTo(matches.get(o2));
            }
        });

        return orderedMatches;
    }

    @Override
    public byte[] serialize() {
        try {
            return GSON.toJson(this).getBytes(CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(CHARSET_NAME);
        }
    }

    private PasswordDatabaseEntry findParent(final PasswordDatabaseEntry target) {
        return Algorithms.breadthFirstSearch(this.entries, new Predicate<PasswordDatabaseEntry>() {
            @Override
            public boolean test(PasswordDatabaseEntry entry) {
                // We have found a match if entry is a folder and contains the target
                return (entry instanceof FolderEntry)
                        && ((FolderEntry) entry).children.contains(target);
            }
        }, GET_CHILDREN);
    }

    /**
     * Deserializes a byte representation of the database (which must have been produced by
     * {@link JsonPasswordDatabase#serialize()}) to produce a new JsonPasswordDatabase object from
     * the representation. The exception to this is that the data produced by {@link #serialize()}
     * may be gzipped, in which case `gzipped` should be set to true.
     *
     * @param data Data to deserialized
     * @param gzipped Whether the data is gzipped
     * @return JsonPasswordDatabase of the given data
     * @throws IOException If the GZIP stream could not be decoded or there was an error processing
     *                     the input stream.
     * @throws JsonSyntaxException If the provided JSON in `data` contained syntactical errors.
     * @throws JsonIOException If there was a problem reading the data
     */
    public static JsonPasswordDatabase deserialize(byte[] data, boolean gzipped)
            throws IOException, JsonSyntaxException, JsonIOException {
        // Form input stream from our byte array, wrapping in GZIP input stream if gzipped
        InputStream input = new ByteArrayInputStream(data);
        if (gzipped)
            input = new GZIPInputStream(input);

        // Create reader to parse our bytes in a set charset
        InputStreamReader inputReader = new InputStreamReader(input, CHARSET_NAME);

        // Parse the JSON using the Gson library.
        return GSON.fromJson(inputReader, JsonPasswordDatabase.class);
    }
}
