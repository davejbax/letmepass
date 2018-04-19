package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Collections;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordFlags;
import uk.co.davidbaxter.letmepass.model.impl.JsonPasswordDatabase;
import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.session.SessionContextRegistry;
import uk.co.davidbaxter.letmepass.session.impl.DefaultSessionContext;
import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.storage.impl.DriveDataStore;
import uk.co.davidbaxter.letmepass.storage.impl.FileDataStore;
import uk.co.davidbaxter.letmepass.util.AsyncUtils;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class CreationViewModel extends ViewModel {

    public static final int ID_STEP_1 = 1;
    public static final int ID_STEP_2 = 2;
    public static final int ID_STEP_3 = 3;
    public static final int ID_STEP_4 = 4;

    /**
     * A live event to signal to the view that it should open dialogs to choose files. If the
     * passed value is true, then this should be for cloud storage. Otherwise, it should be for
     * device storage
     */
    private SingleLiveEvent<Boolean> chooseStorageEvent = new SingleLiveEvent<>();

    /**
     * A live event to signal to the view to open the main activity, as the session has been created
     */
    private SingleLiveEvent<Void> launchMainEvent = new SingleLiveEvent<>();

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

    public LiveData<Void> getLaunchMainEvent() {
        return launchMainEvent;
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

    public void onMpChanged(CharSequence s, int start, int before, int count) {
        // TODO: temporary code here to give a prototype; actual code will call a Service
        PasswordFlags pwdFlags = passwordFlags.getValue();
        if (pwdFlags == null)
            pwdFlags = new PasswordFlags();

        generateFlags(pwdFlags, s.toString());

        // Re-set passwordFlags LiveData so UI updates
        passwordFlags.setValue(pwdFlags);
    }

    public void onDriveCreated(DriveDataStore store) {
        this.cloudDataStore = store;
        AsyncUtils.futureToLiveData(store.getStoreName(), cloudLocation, false).execute();
    }

    public void onFileCreated(FileDataStore store) {
        this.deviceDataStore = store;
        AsyncUtils.futureToLiveData(store.getStoreName(), deviceLocation, false).execute();
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
        // TODO: context.setEncrypter(...)
        // TODO here: create a SessionContextFactory of some sort? Encrypter will need deps at least

        // Set the session context singleton
        SessionContextRegistry.setSessionContext(context);

        // Signal to the view that we are ready to launch the main screen
        launchMainEvent.postValue(null);
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

    public static void generateFlags(PasswordFlags flags, String password) {
        // TODO: remove this temp function; will be implemented by a service in practice
        // Working booleans from which to set the password flags
        boolean numbers = false;
        boolean upper = false;
        boolean lower = false;
        boolean symbols = false;

        // Iterate over characters to check whether containing certain chars
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if ((c >= 32 && c <= 47)
                    || (c >= 58 && c <= 64)
                    || (c >= 91 && c <= 96)
                    || (c >= 123 && c <= 126))
                symbols = true;
            if (c >= 97 && c <= 122)
                lower = true;
            if (c >= 65 && c <= 90)
                upper = true;
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
        flags.notBlacklisted = true;
    }

}
