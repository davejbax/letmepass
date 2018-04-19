package uk.co.davidbaxter.letmepass.util;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A collection of utilities for asynchronous programming
 */
public class AsyncUtils {

    private static final String TAG = "AsyncUtils";

    /**
     * Converts a {@link Future} object into an {@link AsyncTask} that can be executed. Executing
     * this task will, asynchronously, wait for the Future to complete, and then call the given
     * {@link Consumer} with the Future's result.
     * <p>
     * The callback will be executed on the main thread, while the Future's get() is called on a
     * background thread.
     *
     * @param future Future to convert
     * @param callback Callback to execute with Future's result once Future has a result
     * @param <T> Type to be returned by Future/consumed
     * @return An AsyncTask that may be executed (note: no params are required)
     * @see AsyncTask#execute(Object[])
     */
    public static <T> AsyncTask<Void, Void, T> futureToTask(final Future<T> future,
                                                            final Consumer<T> callback) {
        return new AsyncTask<Void, Void, T>() {
            private T result;
            private Throwable exception = null;

            @Override
            protected T doInBackground(Void... voids) {
                try {
                    this.result = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    this.exception = e;

                    // If we have an ExectuionException, get the underlying cause
                    if (e instanceof  ExecutionException) {
                        this.exception = ((ExecutionException) e).getCause();
                    }

                    // Log the error and nullify result
                    Log.e(TAG, "Exception occurred in getting future", e);
                    this.result = null;
                }

                return this.result;
            }

            @Override
            protected void onPostExecute(T t) {
                if (this.exception == null) {
                    callback.accept(t);
                } else {
                    callback.onFailure(this.exception);
                }
            }
        };
    }

    /**
     * Converts a {@link Future} object into an {@link AsyncTask} that can be executed. Executing
     * this task will, asynchronously, wait for the Future to complete, and then will post a value
     * to the provided {@link MutableLiveData}.
     * <p>
     * If the Future fails to complete, the provided
     * MutableLiveData will be set to null if `errorsAsNull` is true. Otherwise, nothing will
     * happen on exception. If it is likely that exceptions will occur, or exceptions should be
     * handled, it may be desirable to use {@link #futureToTask(Future, Consumer)} directly
     * instead.
     *
     * @param future Future to convert
     * @param liveData MutableLiveData to post value of Future to once Future is complete
     * @param errorsAsNull Whether to post a value of null to the `liveData` if the Future fails
     * @param <T> The type returned by the Future
     * @return An AsyncTask that may be executed to wait for the Future on a background thread, and
     *         then post a value to the LiveData on the main thread.
     */
    public static <T> AsyncTask<Void, Void, T> futureToLiveData(final Future<T> future,
                                                                final MutableLiveData<T> liveData,
                                                                final boolean errorsAsNull) {
        return futureToTask(future, new Consumer<T>() {
            @Override
            public void accept(T t) {
                liveData.postValue(t);
            }

            @Override
            public void onFailure(Throwable t) {
                if (errorsAsNull)
                    liveData.postValue(null);
            }
        });
    }

}
