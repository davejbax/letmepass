package uk.co.davidbaxter.letmepass.security;

import android.Manifest;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.davidbaxter.letmepass.util.InstantFuture;

/**
 * A service for checking the number of password breaches that a password has appeared in, securely.
 * <p>
 * This service makes a call to Troy Hunt's Pwned Passwords API V2. The password itself is never
 * sent in this request. Rather, it is hashed, and the first five characters of the 40-character
 * hash are sent to the server; the server then returns a list of hashes that we can filter through.
 * This class abstracts this process, allowing for an asynchronous call to this service.
 */
public class PasswordBreachService {

    /**
     * An unknown error occurred while attempting to check password breaches
     */
    public static final int ERROR_UNKNOWN_ERROR = -1;

    /**
     * An IO exception occurred while checking password breaches. This could occur due to an HTTP
     * connection failure, for instance.
     */
    public static final int ERROR_IO_EXCEPTION = -2;

    /**
     * The app has not been granted permission to use the internet. If this error occurs, the
     * current activity should request the permission android.permission.INTERNET, and then call
     * the service again.
     */
    public static final int ERROR_NO_PERMS = -3;

    /**
     * A server-side error occurred while attempting to check breaches.
     */
    public static final int ERROR_SERVER_ERROR = -4;

    // Class constants
    private static final Charset CHARSET_UTF8 = Charset.forName("utf-8");
    private static final String API_URL = "https://api.pwnedpasswords.com/range/%s";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Context context;

    /**
     * Creates a new PasswordBreachService linked to the given (preferably application) context.
     * @param context Context, preferably the application context
     */
    public PasswordBreachService(Context context) {
        this.context = context;
    }

    /** Checks whether we have permissions to access the internet */
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                == PermissionChecker.PERMISSION_GRANTED;
    }

    /**
     * Checks the number of known breaches that a password has appeared in, asynchronously. Note
     * that this requires the android.permission.INTERNET permission; if this is not found, then
     * the error {@link #ERROR_NO_PERMS} will be returned as an {@link InstantFuture}.
     * <p>
     * This method may fail, in which case it will return any one of the following errors:
     * <ul>
     *     <li>{@link #ERROR_NO_PERMS} if there are insufficient permissions</li>
     *     <li>{@link #ERROR_IO_EXCEPTION} if the breach check service could not be accessed</li>
     *     <li>{@link #ERROR_SERVER_ERROR} if a server-side error occurred</li>
     *     <li>{@link #ERROR_UNKNOWN_ERROR} if the cause of the error is unknown</li>
     * </ul>
     * Otherwise, a value >= 0 will be returned with the number of breaches.
     *
     * @param password The password to check the number of breaches of
     * @return A future that, on completion, gives an error (&lt; 0) or the number of breaches (&gt;= 0)
     */
    public Future<Integer> checkBreaches(final String password) {
        if (!hasPermissions()) {
            return new InstantFuture<>(ERROR_NO_PERMS);
        }

        // Submit the task to check breaches asynchronously
        return executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    // Get the password hash and form URL from first 5 chars of hex hash
                    // N.B. - hash in uppercase because API returns uppercase hashes
                    String passwordHashHex = getSha1Hash(password).toUpperCase();
                    String passwordHashPrefix = passwordHashHex.substring(0, 5);
                    String passwordHashSuffix = passwordHashHex.substring(5);
                    String url = String.format(API_URL, passwordHashPrefix);

                    // Build and send request
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = httpClient.newCall(request).execute();

                    // If something went wrong, fail gracefully
                    if (response.code() != 200) {
                        return ERROR_SERVER_ERROR;
                    }

                    // Decode response using the appropriate charset
                    String responseBody = response.body().string();

                    // Check if the password hash -suffix- is contained
                    int index = -1;
                    if ((index = responseBody.indexOf(passwordHashSuffix)) != -1) {
                        // If suffix is contained, get breach count. Line should be in format:
                        // <hash>:<breaches>\n
                        String breaches = responseBody.substring(
                                index + 36, // 35 chars for hash suffix; 1 char for colon separator
                                responseBody.indexOf('\n', index) // Scan to newline
                        ).trim(); // Trim to remove whitespace

                        return Integer.parseInt(breaches);
                    }

                    // Password hash not found in breached hash list; return 0 breaches
                    return 0;
                } catch (NoSuchAlgorithmException e) {
                    // For some reason we couldn't SHA-1 hash the password; fail gracefully
                    Log.e(PasswordBreachService.class.getSimpleName(), "Failed to get SHA-1", e);
                    return ERROR_UNKNOWN_ERROR;
                } catch (NumberFormatException e) {
                    // We couldn't convert the number of breaches to an int
                    Log.e(PasswordBreachService.class.getSimpleName(), "Malformed response", e);
                    return ERROR_UNKNOWN_ERROR;
                } catch (IOException e) {
                    // Couldn't connect or otherwise HTTP request failed; fail gracefully
                    return ERROR_IO_EXCEPTION;
                }
            }
        });
    }

    private String getSha1Hash(String data) throws NoSuchAlgorithmException {
        // Get the SHA-1 digest of the data
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(data.getBytes(CHARSET_UTF8));

        // Get the digest as a big integer so we can store it as an int to format it to hex
        BigInteger digestInt = new BigInteger(1, digest);
        // Format the digest as hex (%0<length in nibbles>x) and return
        return String.format("%040x", digestInt);
    }

}
