package uk.co.davidbaxter.letmepass.storage.impl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import uk.co.davidbaxter.letmepass.R;

public class DriveStorageService {

    /**
     * The default mime type of new files
     */
    private static final String MIME_TYPE = "application/octet-stream";

    /**
     * Application context from which the drive client will be constructed
     */
    private final Context context;

    // Drive clients
    private GoogleSignInClient signInClient;
    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;

    /**
     * Constructs a new DriveStorageService within the given context. Generally, this should be the
     * application context.
     *
     * @param context Context, preferably an application context
     * @see android.app.Activity#getApplicationContext()
     *
     */
    public DriveStorageService(Context context) {
        this.context = context;
    }

    /**
     * Initializes the Drive client; this MUST be called when an activity is created, or at a
     * similarly appropriate time. The service <b>cannot</b> be used if this method is not called.
     */
    public void onCreate() {
        if (this.signInClient != null)
            return;

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestScopes(Drive.SCOPE_FILE)
                .build();

        this.signInClient = GoogleSignIn.getClient(context, signInOptions);
    }

    /**
     * Checks whether onCreate() has been called
     * @return True if onCreate was called
     */
    public boolean isCreated() {
        return signInClient != null;
    }

    /**
     * Checks whether there has been a successful sign in
     * @return True if signed in
     */
    public boolean isSignedIn() {
        return driveClient != null && driveResourceClient != null;
    }

    /**
     * Gets an {@link Intent} to start in order to allow a user to sign in to Drive. The result of
     * this intent -- if the response code was OK -- should be processed using
     * {@link #onSignInResult(Intent)}.
     *
     * @see android.app.Activity#startActivityForResult(Intent, int)
     * @return Intent to start in the {@link android.app.Activity}
     * */
    public Intent getSignInIntent() {
        if (this.signInClient == null)
            throw new IllegalStateException("No sign in client has been created!");

        return this.signInClient.getSignInIntent();
    }

    /**
     * Processes the result of a sign in intent as created using {@link #getSignInIntent()}.
     * @param data Intent result data
     */
    public void onSignInResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount signInAccount = task.getResult(ApiException.class);

            this.driveClient = Drive.getDriveClient(context, signInAccount);
            this.driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
        } catch (ApiException e) {
            // TODO: handle exception
        }
    }

    /**
     * Gets an {@link IntentSender} to show a file choosing/creation dialog in Drive. The intent
     * will return a result that should be captured in the {@link android.app.Activity}; if
     * successful, the result should be processed using {@link #onCreateFileResult(Intent)}, which
     * will return a DataStore[TODO] for use of the file.
     *
     * @param title Title of file creation dialog
     * @param fileName Default filename to show initially in dialog
     * @param callback Callback to receive the IntentSender, or an exception on failure to create
     *                 the IntentSender.
     */
    public void getCreateFileIntent(String title, String fileName,
                                    final Callback<IntentSender> callback) {
        // Specify options for the file creation dialog
        CreateFileActivityOptions createOptions = new CreateFileActivityOptions.Builder()
            .setActivityTitle(title)
            .setInitialDriveContents(null)
            .setInitialMetadata(new MetadataChangeSet.Builder()
                .setMimeType(MIME_TYPE)
                .setTitle(fileName)
                .build())
            .build();

        // Launch the task to create an IntentSender, and delegate to the given callback.
        driveClient.newCreateFileActivityIntentSender(createOptions)
            .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                @Override
                public void onSuccess(IntentSender intentSender) {
                    callback.onSuccess(intentSender);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Processes a successful file creation intent result. This will produce a DataStore[TODO] which
     * can then be used to interact with the file.
     *
     * @param data The data of the intent result
     * @return TODO
     */
    public DriveDataStore onCreateFileResult(Intent data) {
        return new DriveDataStore(
                this,
                data.<DriveId>getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
        );
    }

    /**
     * Gets an {@link IntentSender} to open an open file dialog for Drive. This is asynchronous,
     * and so a callback is required to receive the result. This may also fail, in which case the
     * failure method of the callback will be invoked.
     * <p>
     * If the creation of the IntentSender is successful, it should be launched with
     * startActivityForResult of {@link android.app.Activity}. The result of this, if the result
     * was OK, should then be processed with {@link #onOpenFileResult(Intent)}.
     *
     * @param callback Callback to use
     */
    public void getOpenFileIntent(final Callback<IntentSender> callback) {
        // Set options for the open file dialog: we filter by extension for now (although we cannot
        // check if titles endWith, just contain, due to API limitations).
        OpenFileActivityOptions openOptions = new OpenFileActivityOptions.Builder()
                .setSelectionFilter(
                        Filters.contains(SearchableField.TITLE, StorageConstants.EXTENSION)) // TODO: don't do this, or enforce extension in creation
                .setActivityTitle(context.getString(R.string.drive_open_file_title))
                .build();

        // Try to create a new open file activity intent sender
        driveClient.newOpenFileActivityIntentSender(openOptions)
                .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                    @Override
                    public void onSuccess(IntentSender intentSender) {
                        callback.onSuccess(intentSender);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Processes the result of an open file intent
     *
     * @param data The data returned by the launched activity
     * @return A {@link DriveDataStore} of the opened file
     */
    public DriveDataStore onOpenFileResult(Intent data) {
        return new DriveDataStore(
                this,
                data.<DriveId>getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
        );
    }

    DriveClient getDriveClient() {
        return driveClient;
    }

    DriveResourceClient getDriveResourceClient() {
        return driveResourceClient;
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

}
