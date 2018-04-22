package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;
import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.session.SessionContextRegistry;
import uk.co.davidbaxter.letmepass.model.SerializationException;
import uk.co.davidbaxter.letmepass.util.AsyncUtils;
import uk.co.davidbaxter.letmepass.util.Consumer;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class DecryptionViewModel extends ViewModel {

    // String resource ID or null for success
    private SingleLiveEvent<Integer> decryptionResult = new SingleLiveEvent<>();

    public MutableLiveData<String> masterPassword = new MutableLiveData<>();
    public MutableLiveData<Boolean> useKeyfile = new MutableLiveData<>();

    public void onDecrypt() {
        // Get our session context: we should have a DataStore in there to read from
        SessionContext sessionContext = SessionContextRegistry.getSessionContext();
        if (sessionContext == null) {
            decryptionResult.postValue(R.string.decryption_error_unknown);
            Log.e(getClass().getSimpleName(), "Session context uninitialized");
            return;
        }

        // Set the MP in the session, and set the session
        String mp = masterPassword.getValue();
        sessionContext.setMasterPassword(mp);
        SessionContextRegistry.setSessionContext(sessionContext);

        // Read and (attempt to) decrypt database
        final Future<Void> decryptTask = sessionContext.readAndDecryptDb();
        AsyncUtils.futureToTask(decryptTask, new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                decryptionResult.postValue(null);
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof DecryptionException) {
                    decryptionResult.postValue(R.string.decryption_failed);
                } else if (t instanceof SerializationException) {
                    decryptionResult.postValue(R.string.decryption_error_corrupt);
                } else {
                    decryptionResult.postValue(R.string.decryption_error_unknown);
                    Log.e(getClass().getSimpleName(), "Decryption unknown exception", t);
                }
            }
        }).execute();
    }

    public LiveData<Integer> getDecryptionResult() {
        return decryptionResult;
    }

}
