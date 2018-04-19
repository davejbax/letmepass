package uk.co.davidbaxter.letmepass.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface DataStore {

    @Nullable byte[] getCachedData();

    @Nullable String getCachedStoreName();

    Future<String> getStoreName();

    Future<byte[]> readData();

    Future<Void> writeData(@NonNull byte[] data);

    Future<Void> deleteStore();

}
