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

public class PasswordDatabaseEntryTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        // If this is not a PDE, we delegate to Gson
        if (!type.getRawType().isAssignableFrom(PasswordDatabaseEntry.class))
            return delegate;

        return (TypeAdapter<T>) newPdeAdapter(gson);
    }

    private TypeAdapter<PasswordDatabaseEntry> newPdeAdapter(Gson gson) {
        final TypeAdapter<FolderEntry> folderDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(FolderEntry.class));
        final TypeAdapter<DataEntry> dataDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(DataEntry.class));
        final TypeAdapter<PasswordEntry> passwordDelegate = gson.getDelegateAdapter(this,
                TypeToken.get(PasswordEntry.class));

        return new TypeAdapter<PasswordDatabaseEntry>() {
            @Override
            public void write(JsonWriter out, PasswordDatabaseEntry value) throws IOException {
                out.beginArray();
                out.value(value.getType());

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

                delegate.write(out, value);
                out.endArray();
            }

            @Override
            public PasswordDatabaseEntry read(JsonReader in) throws IOException {
                in.beginArray();
                String type = in.nextString();

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

                PasswordDatabaseEntry entry = (PasswordDatabaseEntry) delegate.read(in);
                in.endArray();

                return entry;
            }
        };
    }
}
