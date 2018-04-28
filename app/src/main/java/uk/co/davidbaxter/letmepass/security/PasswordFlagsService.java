package uk.co.davidbaxter.letmepass.security;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.PasswordFlags;

public class PasswordFlagsService {

    /** File name of blacklist file when stored as a copy in the app storage */
    private static final String BLACKLIST_FILE_NAME = "blacklist.db";

    /** Connection to the blacklist database */
    private static Connection blacklistConnection = null;

    /** Application context referecne */
    private Context context;

    public PasswordFlagsService(Context context) {
        this.context = context;

        try {
            loadBlacklist(context);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Failed to load password blacklist", e);
        }
    }

    /**
     * Gets a new PasswordFlags instance from a password. This method involves the overhead of
     * instantiating a {@link PasswordFlags} instance each time.
     *
     * @param password Password for which to create the flags
     * @return {@link PasswordFlags} instance for the password
     */
    public PasswordFlags getFlags(String password) {
        PasswordFlags flags = new PasswordFlags();
        updateFlags(flags, password);
        return flags;
    }

    /**
     * Updates an existing PasswordFlags instance based on a new password
     *
     * @param flags Flags instance to update (modify)
     * @param password Password for which to calculate flags
     */
    public void updateFlags(PasswordFlags flags, String password) {
        if (password.isEmpty()) {
            flags.hasSymbols = false;
            flags.hasMixedChars = false;
            flags.goodLength = false;
            flags.notBlacklisted = false;
            return;
        }

        boolean numbers = false;
        boolean upper = false;
        boolean lower = false;
        boolean symbols = false;

        // Iterate over characters to check whether containing certain chars
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            // Check symbols ranges
            if ((c >= 32 && c <= 47)
                    || (c >= 58 && c <= 64)
                    || (c >= 91 && c <= 96)
                    || (c >= 123 && c <= 126))
                symbols = true;

            // Check lower range
            if (c >= 97 && c <= 122)
                lower = true;

            // Check upper range
            if (c >= 65 && c <= 90)
                upper = true;

            // Check numbers range
            if (c >= 48 && c <= 57)
                numbers = true;
        }

        // Set good length
        flags.goodLength = password.length() >= 12;

        // Set mixed chars
        flags.hasMixedChars = upper && lower && numbers;

        // Set symbols
        flags.hasSymbols = symbols;

        // Set blacklisted
        flags.notBlacklisted = !isBlacklisted(password);
    }

    /**
     * Loads the blacklist database file from the application resources and/or the app's storage.
     * <p>
     * This method loads the database by copying it from the app resources to the app's data
     * directory in internal storage. As such, it can fail, in which case the blacklist will be
     * uninitialized and the 'notBlacklisted' flag will always be true.
     *
     * @param context Application context
     * @throws IOException if an error occurred while copying the blacklist database file from
     *                     resources to disk
     * @throws SQLException if the database could not be loaded
     */
    public static void loadBlacklist(Context context) throws IOException, SQLException {
        // If we've already loaded the blacklist, do nothing
        if (blacklistConnection != null)
            return;

        File dbFile = new File(context.getFilesDir(), BLACKLIST_FILE_NAME);

        // Check that the DB file exists in local storage
        if (!dbFile.exists()) {
            // Write the DB file to a file in storage so we can read from it
            InputStream in = context.getResources().openRawResource(R.raw.blacklist_db);
            FileOutputStream out = new FileOutputStream(dbFile, false); // false = don't append
            IOUtils.copyStream(in, out);
        }

        // Register JDBC driver
        try {
            DriverManager.registerDriver(
                    (Driver) Class.forName("org.sqldroid.SQLDroidDriver").newInstance());
        } catch (Exception e) {
            Log.e(PasswordFlagsService.class.getSimpleName(), "Failed to register driver", e);
        }

        // Load the database and save the connection
        String jdbcUrl = "jdbc:sqldroid:" + dbFile.getPath();
        blacklistConnection = DriverManager.getConnection(jdbcUrl);
    }

    /**
     * Releases occupied resources of the blacklist database
     */
    public static void closeBlacklist() {
        // Close the connection if it exists
        if (blacklistConnection != null)
            try {
                blacklistConnection.close();
            } catch (Exception e) {}
    }

    private boolean isBlacklisted(String password) {
        if (password.isEmpty() || blacklistConnection == null)
            return false;

        // Prepare query parameters
        char ch = password.charAt(0);
        int length = password.length();
        String suffix = password.substring(1);

        try {
            // Prepare a statement to search the blacklist DB for this password
            PreparedStatement stmnt = blacklistConnection.prepareStatement(String.format(
                Locale.ENGLISH,
                "SELECT suffix FROM blacklist WHERE first_letter = '%c'" +
                " AND suffix = '%s'" +
                " AND length = %d",
                ch, suffix, length)
            );

            // Execute the query
            ResultSet rs = stmnt.executeQuery();

            // Return true if we have any results, false otherwise
            return rs.next();
        } catch (SQLException e) {
            // Return false if we have an SQL exception
            return false;
        }
    }

}
