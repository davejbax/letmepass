package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.security.Security;
import java.util.Collections;
import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordFlags;
import uk.co.davidbaxter.letmepass.model.impl.JsonPasswordDatabase;
import uk.co.davidbaxter.letmepass.security.SecurityServices;
import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.session.SessionContextRegistry;
import uk.co.davidbaxter.letmepass.session.impl.DefaultSessionContext;
import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.storage.impl.DriveDataStore;
import uk.co.davidbaxter.letmepass.storage.impl.FileDataStore;
import uk.co.davidbaxter.letmepass.model.impl.VersionedEncryptedDatabaseSerializer;
import uk.co.davidbaxter.letmepass.ui.CreationActivity;
import uk.co.davidbaxter.letmepass.util.AsyncUtils;
import uk.co.davidbaxter.letmepass.util.Consumer;
import uk.co.davidbaxter.letmepass.util.DebounceUtils;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class CreationViewModel extends ViewModel {

    public static final int ID_STEP_1 = 1;
    public static final int ID_STEP_2 = 2;
    public static final int ID_STEP_3 = 3;
    public static final int ID_STEP_4 = 4;

    private static final String DEBOUNCE_KEY_FLAGS = "FLAGS";
    private static final int DEBOUNCE_TIME_MS = 400;

    /**
     * A live event to signal to the view that it should open dialogs to choose files. If the
     * passed value is true, then this should be for cloud storage. Otherwise, it should be for
     * device storage
     */
    private SingleLiveEvent<Boolean> chooseStorageEvent = new SingleLiveEvent<>();

    /**
     * A live event to signal to the view to open the main activity, as the session has been
     * created. True means that creation was successful, false means it was not and the user should
     * be informed of this.
     */
    private SingleLiveEvent<Boolean> createResult = new SingleLiveEvent<>();

    // Bound fields: these are set by the view, and can be read/set by us
    public MutableLiveData<String> masterPassword = new MutableLiveData<>();
    public MutableLiveData<String> masterPasswordAgain = new MutableLiveData<>(); // Re-entry of mp
    public MutableLiveData<String> dbName = new MutableLiveData<>();
    public MutableLiveData<Boolean> dbIsDefault = new MutableLiveData<>();

    // Observable fields: the view observes these fields and updates accordingly. For instance,
    // to disable a portion of the UI when a different option is selected.
    public MutableLiveData<Boolean> cloudChecked = new MutableLiveData<>();
    public MutableLiveData<String> cloudLocation = new MutableLiveData<>();
    public MutableLiveData<String> deviceLocation = new MutableLiveData<>();
    public MutableLiveData<String> keyfileLocation = new MutableLiveData<>();
    public MutableLiveData<PasswordFlags> passwordFlags = new MutableLiveData<>();

    // Parameters from which to construct session
    private DataStore cloudDataStore = null;
    private DataStore deviceDataStore = null;

    public CreationViewModel() {
        this.cloudChecked.setValue(true);
    }

    public void setCloudChecked(boolean cloudChecked) {
        this.cloudChecked.postValue(cloudChecked);
    }

    // TODO doc
    public LiveData<Boolean> getChooseStorageEvent() {
        return chooseStorageEvent;
    }

    // TODO doc
    public LiveData<Boolean> getCreateResult() {
        return createResult;
    }

    public void onOpenCloud() {
        chooseStorageEvent.postValue(true);
    }

    public void onOpenDevice() {
        chooseStorageEvent.postValue(false);
    }

    public void onBreachCheck() {
        // TODO
    }

    public void onBreachCheckHelp() {
        // TODO
    }

    public void onGenKeyfile() {
        // TODO
    }

    public void onMpChanged(final CharSequence s, int start, int before, int count) {
        final PasswordFlags pwdFlags = passwordFlags.getValue() == null ? new PasswordFlags() :
                passwordFlags.getValue();

        // Debounce to avoid superfluous calls to flags (async)
        DebounceUtils.debounce(this, DEBOUNCE_KEY_FLAGS, DEBOUNCE_TIME_MS, new Runnable() {
            @Override
            public void run() {
                // If no security services instance, just return
                if (SecurityServices.getInstance() == null)
                    return;

                // Update the password flags with the new master password, posting result to UI
                SecurityServices.getInstance()
                        .getPasswordFlagsService()
                        .updateFlags(pwdFlags, s.toString());
                passwordFlags.postValue(pwdFlags);
            }
        });
    }

    public void onDriveCreated(DriveDataStore store) {
        // Delete the cloud store if it exists: otherwise we'll have a bunch of empty files
        if (this.cloudDataStore != null)
            this.cloudDataStore.deleteStore();

        this.cloudDataStore = store;
        AsyncUtils.futureToLiveData(store.getStoreName(), cloudLocation, false).execute();
    }

    public void onFileCreated(FileDataStore store) {
        this.deviceDataStore = store;
        AsyncUtils.futureToLiveData(store.getStoreName(), deviceLocation, false).execute();
    }

    public void onCancel() {
        // Delete the cloud store if it exists: cloud store makes a blank file due to its nature
        if (cloudDataStore != null)
            cloudDataStore.deleteStore();
    }

    public void onComplete() {
        // Create the database
        PasswordDatabase database = new JsonPasswordDatabase(dbName.getValue() == null ? ""
                : dbName.getValue(), Collections.<PasswordDatabaseEntry>emptyList());

        // Get the datastore so we can write database
        DataStore dataStore = cloudChecked.getValue() != null && cloudChecked.getValue() ?
                cloudDataStore : deviceDataStore;

        SessionContext context = new DefaultSessionContext();
        context.setDataStore(dataStore);
        context.setDatabase(database);
        context.setMasterPassword(masterPassword.getValue());
        context.setEncryptedDatabaseSerializer(new VersionedEncryptedDatabaseSerializer());
        SessionContextRegistry.setSessionContext(context);

        // Encrypt and save the database
        Future<Void> future = context.encryptAndSaveDb();
        AsyncUtils.futureToTask(future, new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                // Signal to the view we are ready to launch the main screen
                createResult.postValue(true);
            }

            @Override
            public void onFailure(Throwable t) {
                createResult.postValue(false);
                Log.e(CreationViewModel.class.getSimpleName(), "Failed to create DB", t);
            }
        }).execute();
        // TODO here: create a SessionContextFactory of some sort? Might be cleaner
    }

    public @Nullable Pair<Integer, Object[]> onVerifyStep(int stepId) {
        switch (stepId) {
            case ID_STEP_1:
                return verifyStep1();
            case ID_STEP_2:
                return verifyStep2();
            case ID_STEP_3:
                return verifyStep3();
            case ID_STEP_4:
                return verifyStep4();
            default:
                // We don't know this step. Assume no error?
                return null;
        }
    }

    public @Nullable Pair<Integer, Integer> generateBlockingDialog(int stepId) {
        switch (stepId) {
            case ID_STEP_2:
                // Step 2: we verify that our optional password flags are reasonable, and if not,
                // we warn the user with this dialog.
                if (passwordFlags.getValue() != null) {
                    // If the password is blacklisted, show them a very stern warning
                    if (!passwordFlags.getValue().notBlacklisted)
                        return Pair.create(R.string.creation_dialog_warning_title,
                                R.string.creation_warning_mp_blacklisted);
                    // If the password does not have mixed chars AND does not have symbols, warn.
                    else if (!passwordFlags.getValue().hasMixedChars
                            && !passwordFlags.getValue().hasSymbols)
                        return Pair.create(R.string.creation_dialog_warning_title,
                                R.string.creation_warning_mp_low_entropy);
                }
        }

        return null;
    }

    private @Nullable Pair<Integer, Object[]> verifyStep1() {
        if (cloudChecked.getValue() != null && cloudChecked.getValue()) {
            // If cloud is checked, we need to have a cloud data store
            if (cloudDataStore == null)
                return Pair.create(R.string.creation_error_no_cloud_location_chosen,
                        new Object[] {});
        } else {
            // If device is checked, we need to have a device data store
            if (deviceDataStore == null)
                return Pair.create(R.string.creation_error_no_device_location_chosen,
                        new Object[] {});
        }

        return null;
    }

    private @Nullable Pair<Integer, Object[]> verifyStep2() {
        if (masterPassword.getValue() == null) {
            return Pair.create(R.string.creation_error_no_mp, new Object[] {});
        } else if (passwordFlags.getValue() == null || !passwordFlags.getValue().goodLength) {
            return Pair.create(R.string.creation_error_mp_too_short, new Object[] {});
        }
        return null;
    }

    private @Nullable Pair<Integer, Object[]> verifyStep3() {
        // No verification needed
        return null;
    }

    private @Nullable Pair<Integer, Object[]> verifyStep4() {
        // Ensure the master password entered again is EXACTLY the same as the master password
        // entered in the other step.
        if (masterPasswordAgain.getValue() == null
                || masterPassword.getValue() == null
                || !masterPasswordAgain.getValue().equals(masterPassword.getValue())) {
            return Pair.create(R.string.creation_error_mp_again_wrong, new Object[] {});
        }
        return null;
    }

}
