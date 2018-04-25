package uk.co.davidbaxter.letmepass.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import uk.co.davidbaxter.letmepass.storage.StorageConstants;

/**
 * A service for obtaining {@link PasswordGenerator} objects
 */
public class PasswordGeneratorService {

    private Context context;
    private PasswordGenerator passwordGenerator;

    public PasswordGeneratorService(Context context) {
        this.context = context;
    }

    /**
     * Retrieves the password generator instance based on settings, or creates one if there is none
     * @return Password generator
     */
    public PasswordGenerator getPasswordGenerator() {
        if (passwordGenerator == null)
            reloadFromSettings();

        return passwordGenerator;
    }

    /**
     * Recreates the password generator using the settings in the app's {@link SharedPreferences}
     */
    public void reloadFromSettings() {
        // Load settings from shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int minLength = prefs.getInt(StorageConstants.PREFS_GEN_MIN_LEN,
                StorageConstants.PREFS_GEN_MIN_LEN_DEF);
        int maxLength = prefs.getInt(StorageConstants.PREFS_GEN_MAX_LEN,
                StorageConstants.PREFS_GEN_MAX_LEN_DEF);
        boolean upper = prefs.getBoolean(StorageConstants.PREFS_GEN_UPPER,
                StorageConstants.PREFS_GEN_UPPER_DEF);
        boolean lower = prefs.getBoolean(StorageConstants.PREFS_GEN_LOWER,
                StorageConstants.PREFS_GEN_LOWER_DEF);
        boolean symbols = prefs.getBoolean(StorageConstants.PREFS_GEN_SYMBOLS,
                StorageConstants.PREFS_GEN_SYMBOLS_DEF);
        boolean numbers = prefs.getBoolean(StorageConstants.PREFS_GEN_NUMBERS,
                StorageConstants.PREFS_GEN_NUMBERS_DEF);

        // Construct a new password generator
        this.passwordGenerator = new PasswordGenerator(minLength, maxLength, symbols, numbers,
                upper, lower);
    }

}
