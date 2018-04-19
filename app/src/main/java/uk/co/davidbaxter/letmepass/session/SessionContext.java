package uk.co.davidbaxter.letmepass.session;

import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.storage.DataStore;

public interface SessionContext {

    PasswordDatabase getDatabase();

    void setDatabase(PasswordDatabase database);

    DataStore getDataStore();

    void setDataStore(DataStore store);

    /* Encrypter getEncrypter(); */

    /* void setEncrypter(Encrypter enc); */

    String getMasterPassword();

    void setMasterPassword(String mp);

    /* Future<Void> encryptAndSaveDb(); */

    /* Future<...> readAndDecryptDb(); */

}
