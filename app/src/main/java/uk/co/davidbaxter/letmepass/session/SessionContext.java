package uk.co.davidbaxter.letmepass.session;

import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.model.EncryptedDatabaseSerializer;

public interface SessionContext {

    PasswordDatabase getDatabase();

    void setDatabase(PasswordDatabase database);

    DataStore getDataStore();

    void setDataStore(DataStore store);

    EncryptedDatabaseSerializer getEncryptedDatabaseSerializer();

    void setEncryptedDatabaseSerializer(EncryptedDatabaseSerializer serializer);

    String getMasterPassword();

    void setMasterPassword(String mp);

    Future<Void> encryptAndSaveDb();

    Future<Void> readAndDecryptDb();

}
