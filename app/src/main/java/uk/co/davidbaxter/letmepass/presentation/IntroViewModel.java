package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.session.SessionContextRegistry;
import uk.co.davidbaxter.letmepass.session.impl.DefaultSessionContext;
import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.model.impl.VersionedEncryptedDatabaseSerializer;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class IntroViewModel extends ViewModel {

    private SingleLiveEvent<Action> action = new SingleLiveEvent<>();

    /**
     * Gets the LiveData holding the selected action to perform
     * @return Action LiveData
     */
    public LiveData<Action> getAction() {
        return action;
    }

    public void onNewDatabase() {
        action.postValue(Action.NEW_DATABASE);
    }

    public void onLoadCloud() {
        action.postValue(Action.LOAD_CLOUD);
    }

    public void onLoadDevice() {
        action.postValue(Action.LOAD_DEVICE);
    }

    public void onDatabaseOpened(DataStore store) {
        // Create session context and store in registry
        SessionContext sessionContext = new DefaultSessionContext();
        sessionContext.setDataStore(store);
        sessionContext.setEncryptedDatabaseSerializer(new VersionedEncryptedDatabaseSerializer());
        SessionContextRegistry.setSessionContext(sessionContext);

        // Launch the decryption screen
        action.postValue(Action.LAUNCH_DECRYPTION);
    }

    /**
     * Enum of actions that may be performed by the view
     */
    public enum Action {
        /** Create a new database */
        NEW_DATABASE,
        /** Load a cloud database */
        LOAD_CLOUD,
        /** Load a device database */
        LOAD_DEVICE,
        /** Launch a decryption screen */
        LAUNCH_DECRYPTION
    }

}
