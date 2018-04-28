package uk.co.davidbaxter.letmepass.model.impl;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;

/**
 * A {@link TypeAdapterFactory} for {@link PasswordDatabaseEntry} classes.
 * <p>
 * This class is used in Gson (de)serialization, as the serialization of PasswordDatabaseEntry
 * classes must be such that they are deserialized correctly into the appropriate subclass. The
 * class also adds a type in the JSON to identify the subclass (since this information is not
 * encoded otherwise).
 */
public class PasswordDatabaseEntryTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        // If this is not a PDE, we delegate to Gson
        if (!type.getRawType().isAssignableFrom(PasswordDatabaseEntry.class))
            return delegate;

        return (TypeAdapter<T>) newPdeAdapter(gson);
    }

    /**
     * Creates a PasswordDatabaseEntry {@link com.google.gson.TypeAdapter} for the given Gson.
     * <p>
     * This TypeAdapter writes and reads JSON for PasswordDatabaseEntry classes. An array is used
     * to store first the type of the entry and next the entry itself. Reading and writing of the
     * JSON itself for the subclasses of PDE is done by delegation to Gson.
     *
     * @param gson Gson instance for which to create the adapter
     * @return TypeAdapter for PDEs
     */
    private TypeAdapter<PasswordDatabaseEntry> newPdeAdapter(Gson gson) {
        // Get all of the delegates for each type
        final TypeAdapter<FolderEntry> folderDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(FolderEntry.class));
        final TypeAdapter<DataEntry> dataDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(DataEntry.class));
        final TypeAdapter<PasswordEntry> passwordDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(PasswordEntry.class));

        return new TypeAdapter<PasswordDatabaseEntry>() {
            @Override
            public void write(JsonWriter out, PasswordDatabaseEntry value) throws IOException {
                out.beginArray(); // Start writing an array
                out.value(value.getType()); // Write first value in array (type name)

                // Deduce the correct delegate TypeAdapter to use
                TypeAdapter delegate = null;
                switch (value.getType()) {
                    case FolderEntry.TYPE:
                        delegate = folderDelegate;
                        break;
                    case DataEntry.TYPE:
                        delegate = dataDelegate;
                        break;
                    case PasswordEntry.TYPE:
                        delegate = passwordDelegate;
                        break;
                }

                // Delegate writing the entry's fields to the delegate adapter
                delegate.write(out, value);
                out.endArray();
            }

            @Override
            public PasswordDatabaseEntry read(JsonReader in) throws IOException {
                in.beginArray(); // Begin reading an array
                String type = in.nextString(); // Read type of entry

                // Deduce delegate to use from found type
                TypeAdapter delegate = null;
                switch (type) {
                    case FolderEntry.TYPE:
                        delegate = folderDelegate;
                        break;
                    case DataEntry.TYPE:
                        delegate = dataDelegate;
                        break;
                    case PasswordEntry.TYPE:
                        delegate = passwordDelegate;
                        break;
                }

                // Read entry using delegate TypeAdapter
                PasswordDatabaseEntry entry = (PasswordDatabaseEntry) delegate.read(in);
                in.endArray();

                return entry;
            }
        };
    }
}
