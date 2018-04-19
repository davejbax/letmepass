package uk.co.davidbaxter.letmepass.session.impl;

import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.session.SessionContext;
import uk.co.davidbaxter.letmepass.storage.DataStore;

public class DefaultSessionContext implements SessionContext {

    private PasswordDatabase database = null;

    private DataStore dataStore = null;

    private String masterPassword = null;

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

}
