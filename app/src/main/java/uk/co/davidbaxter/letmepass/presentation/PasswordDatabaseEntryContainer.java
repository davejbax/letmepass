package uk.co.davidbaxter.letmepass.presentation;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;

/**
 * Container class to help in presentation of PasswordDatabaseEntries.
 * <p>
 * In particular, this class determines the icons, and can also represent a divider, allowing these
 * containers to be stored in a contiguous list to be presented (a divider is a dummy list item that
 * contains no entry, but rather represents a division in the types of underlying entries, which may
 * be displayed).
 */
public class PasswordDatabaseEntryContainer {

    private final PasswordDatabaseEntry entry;
    private final boolean isDivider;
    private final int dividerStringId;

    // Private (convenience) constructor to set all required members; this keeps code DRY
    private PasswordDatabaseEntryContainer(@Nullable PasswordDatabaseEntry entry,
                                           int dividerStringId,
                                           boolean isDivider) {
        this.entry = entry;
        this.dividerStringId = dividerStringId;
        this.isDivider = isDivider;
    }

    /**
     * Construct a new container that contains a password database entry.
     *
     * @param entry Entry to contain
     */
    public PasswordDatabaseEntryContainer(@NonNull PasswordDatabaseEntry entry,
                                          LiveData<SortingCriteria> sortingCriteria) {
        // We are a container for an entry; invoke constructor with correct parameters
        this(entry, 0, false);
    }

    /**
     * Construct a new container that represents a divider to categorize passwords/folders/etc.
     *
     * @param dividerStringId The (string) resource ID of the divider's title
     */
    public PasswordDatabaseEntryContainer(int dividerStringId) {
        // We are a divider; invoke constructor with correct parameters
        this(null, dividerStringId, true);
    }

    /**
     * Get the drawable resource ID for the icon of this entry
     *
     * @return Resource ID
     */
    public int getIconId() {
        // If no entry, return password symbol; this does not particularly matter as the function
        // should not be invoked if there is no entry
        if (entry == null)
            return R.drawable.ic_vpn_key_black_24dp;

        // Compare entry type; we use this rather than the class type itself to sustain the liberty
        // of polymorphism -- i.e. classes needn't be a strict type, as long as they claim to be
        // a certain type (which corresponds to our default as specified in our default impl.s)
        switch (entry.getType()) {
            case PasswordEntry.TYPE:
                return R.drawable.ic_vpn_key_black_24dp;
            case FolderEntry.TYPE:
                return R.drawable.ic_folder_black_24dp;
            case DataEntry.TYPE:
                return R.drawable.ic_insert_drive_file_black_24dp;
        }

        // Default to password symbol, even if not strictly a password
        return R.drawable.ic_vpn_key_black_24dp;
    }

    /**
     * Get the entry that this container holds. This may be null: if so, the container is likely
     * a divider of some sort.
     * @return
     */
    public PasswordDatabaseEntry getEntry() {
        return entry;
    }

    /**
     * Returns true if this container contains no entry and instead represents a divider, and false
     * otherwise.
     *
     * @return Whether the container is a divider
     */
    public boolean isDivider() {
        return isDivider;
    }

    /**
     * Gets the resource ID for the divider's title.
     *
     * @return Resource ID
     */
    public int getDividerStringId() {
        return dividerStringId;
    }

    /**
     * Gets the title to be shown for this entry
     * @return Entry title to display
     */
    public String getEntryTitle() {
        return entry == null ? "" : entry.name;
    }

    public String getEntrySubtitle(Context context) {
        if (entry == null)
            return "";

        String subtitle = "";

        // Set subtitle to:
        // - 'x passwords' if folder;
        // - 'Username: x' if password;
        // - 'Text data' if data
        if (entry.getType().equals(FolderEntry.TYPE)) {
            FolderEntry folder = (FolderEntry) entry;
            subtitle = context.getString(
                    R.string.main_entry_subtitle_folder,
                    context.getResources().getQuantityString(
                            R.plurals.passwords,
                            folder.getEntryCount(),
                            folder.getEntryCount()
                    )
            );
        } else if (entry.getType().equals(PasswordEntry.TYPE)) {
            PasswordEntry password = (PasswordEntry) entry;
            subtitle = context.getString(
                    R.string.main_entry_subtitle_password,
                    password.username
            );
        } else if (entry.getType().equals(DataEntry.TYPE)) {
            subtitle = context.getString(R.string.main_entry_subtitle_data);
        } else {
            // Unknown type, return blank string
            subtitle = "";
        }

        // We have separate formatting if the entry is a favourite.
        if (entry.favorite) {
            return context.getString(R.string.main_entry_subtitle_fav, subtitle);
        } else {
            return subtitle;
        }
    }

    // Override equals to allow for comparison on values
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PasswordDatabaseEntryContainer)) // Or null (implicit)
            return false;

        PasswordDatabaseEntryContainer otherContainer = (PasswordDatabaseEntryContainer) other;

        return otherContainer.isDivider == isDivider                 // Same type? (divider or not)
                && otherContainer.dividerStringId == dividerStringId // Same divider string?
                && (otherContainer.entry == null ? (entry == null)   // Entry is null (divider), or
                    : otherContainer.entry.equals(entry));           // entry is equivalent
    }
}
