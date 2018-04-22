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
        SessionContext sessionContext = new DefaultSessionContext();
        sessionContext.setDataStore(store);
        sessionContext.setEncryptedDatabaseSerializer(new VersionedEncryptedDatabaseSerializer());
        SessionContextRegistry.setSessionContext(sessionContext);
        action.postValue(Action.LAUNCH_DECRYPTION);
    }

    public enum Action {
        NEW_DATABASE,
        LOAD_CLOUD,
        LOAD_DEVICE,
        LAUNCH_DECRYPTION
    }

}
