package uk.co.davidbaxter.letmepass.session.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.model.EncryptedDatabaseSerializer;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.storage.DataStore;

public class DefaultSessionContext implements SessionContext {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Object databaseLock = new Object();

    private PasswordDatabase database = null;

    private DataStore dataStore = null;

    private String masterPassword = null;

    private EncryptedDatabaseSerializer serializer = null;

    @Override
    public PasswordDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(PasswordDatabase database) {
        this.database = database;
    }

    @Override
    public DataStore getDataStore() {
        return dataStore;
    }

    @Override
    public void setDataStore(DataStore store) {
        this.dataStore = store;
    }

    @Override
    public String getMasterPassword() {
        return masterPassword;
    }

    @Override
    public void setMasterPassword(String mp) {
        this.masterPassword = mp;
    }

    @Override
    public EncryptedDatabaseSerializer getEncryptedDatabaseSerializer() {
        return serializer;
    }

    @Override
    public void setEncryptedDatabaseSerializer(EncryptedDatabaseSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Future<Void> encryptAndSaveDb() {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                synchronized (databaseLock) {
                    byte[] db = serializer.serialize(database, masterPassword);
                    Future<Void> writeFuture = dataStore.writeData(db);
                    return writeFuture.get();
                }
            }
        });
    }

    @Override
    public Future<Void> readAndDecryptDb() {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                synchronized (databaseLock) {
                    Future<byte[]> readFuture = dataStore.readData();
                    byte[] data = readFuture.get();
                    database = serializer.deserialize(data, masterPassword);
                    return null;
                }
            }
        });
    }
}
