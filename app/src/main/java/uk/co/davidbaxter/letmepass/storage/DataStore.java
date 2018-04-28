package uk.co.davidbaxter.letmepass.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * A named store of data that may be written to, read from, or deleted.
 */
public interface DataStore {

    /**
     * Gets the current cached data, if there is any. This usually requires at least one call to
     * {@link #readData()}.
     * @return Cached data, or null if there is none
     */
    @Nullable byte[] getCachedData();

    /**
     * Gets the current cached store name, if there is any. This usually requires at least one call
     * to {@link #getStoreName()}.
     * @return Cached store name, or null if none
     */
    @Nullable String getCachedStoreName();

    /**
     * Gets a name representing this particular data store instance asynchronously
     * @return Future that will hold name of store
     */
    Future<String> getStoreName();

    /**
     * Reads data from the store asynchronously
     * @return Future that will hold read data, or throw an ExecutionException if the data could not
     *         be read.
     */
    Future<byte[]> readData();

    /**
     * Writes data to the store asynchronously
     * @param data Data to write
     * @return A future that will complete once the data has been written
     */
    Future<Void> writeData(@NonNull byte[] data);

    /**
     * Deletes the data store permanently (async). You should not attempt to call any of the
     * non-cached methods of this class after calling this.
     * @return A future that will complete once the data store has been deleted.
     */
    Future<Void> deleteStore();

}
