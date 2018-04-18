package uk.co.davidbaxter.letmepass.storage.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.davidbaxter.letmepass.storage.DataStore;

public class DriveDataStore implements DataStore {

    private byte[] cachedData = null;
    private String cachedStoreName = null;

    private DriveStorageService service;
    private DriveFile file;

    public DriveDataStore(DriveStorageService service, DriveId fileId) {
        this.service = service;
        this.file = fileId.asDriveFile();
    }

    @Nullable
    @Override
    public byte[] getCachedData() {
        return cachedData;
    }

    @Override
    public String getCachedStoreName() {
        return cachedStoreName;
    }

    @Override
    public Future<String> getStoreName() {
        Task<Metadata> metadataTask = service.getDriveResourceClient().getMetadata(file);
        FutureTaskLink<Metadata> metadataFuture = new FutureTaskLink<>(metadataTask);
        return new FutureTransformer<Metadata, String>(metadataFuture) {
            @Override
            protected String transform(Metadata meta) throws Exception {
                String title = meta.getTitle();

                // Update our cache while we're here
                DriveDataStore.this.cachedStoreName = title;

                // Return title
                return title;
            }
        };
    }

    @Override
    public Future<byte[]> readData() {
        Future<DriveContents> openFileFuture = openFile(DriveFile.MODE_READ_ONLY);
        return new FutureTransformer<DriveContents, byte[]>(openFileFuture) {
            @Override
            protected byte[] transform(DriveContents contents) throws IOException {
                // Read input stream using IOUtils
                InputStream in = contents.getInputStream();
                byte[] result = IOUtils.toByteArray(in);

                // Update our cache while we're here
                DriveDataStore.this.cachedData = result;

                return result;
            }
        };
    }

    @Override
    public Future<Void> writeData(@NonNull final byte[] data) {
        Task<DriveContents> openFileTask =
                service.getDriveResourceClient().openFile(file, DriveFile.MODE_WRITE_ONLY);

        Task<Void> writeDataTask = openFileTask.continueWithTask(
                new Continuation<DriveContents, Task<Void>>() {

            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents contents = task.getResult();

                // Copy provided bytes to the output stream of the file
                InputStream in = new ByteArrayInputStream(data);
                OutputStream out = contents.getOutputStream();
                IOUtils.copyStream(in, out);

                // Commit the changes to the file
                return service.getDriveResourceClient().commitContents(contents, null);
            }

        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                // Update the cache if we succeed
                DriveDataStore.this.cachedData = data;
            }
        });

        return new FutureTaskLink<>(writeDataTask);
    }

    @Override
    public Future<Void> deleteStore() {
        Task<Void> deleteFileTask = service.getDriveResourceClient().trash(file);
        return new FutureTaskLink<>(deleteFileTask);
    }

    private Future<DriveContents> openFile(int mode) {
        Task<DriveContents> openFileTask =
                service.getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        return new FutureTaskLink<>(openFileTask);
    }

    private static class FutureTaskLink<T> implements Future<T>, OnSuccessListener<T>,
            OnFailureListener, OnCanceledListener {

        private Task<T> task;
        private T value = null;
        private Exception exception = null;
        private boolean cancelled = false;
        private final Object notifyObj = new Object();

        public FutureTaskLink(Task<T> task) {
            this.task = task;
            this.task.addOnSuccessListener(this);
            this.task.addOnFailureListener(this);
            this.task.addOnCanceledListener(this);
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            this.exception = e;
            synchronized (this.notifyObj) {
                this.notifyObj.notifyAll();
            }
        }

        @Override
        public void onSuccess(T t) {
            this.value = t;
            synchronized (this.notifyObj) {
                this.notifyObj.notifyAll();
            }
        }

        @Override
        public void onCanceled() {
            this.cancelled = true;
            synchronized (this.notifyObj) {
                this.notifyObj.notifyAll();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) {
                this.cancelled = true;
                synchronized (this.notifyObj) {
                    this.notifyObj.notifyAll();
                }
                return true;
            }

            return false; // We cannot cancel tasks
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public boolean isDone() {
            return this.task.isComplete();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (!this.task.isComplete()) {
                // Wait until the task has completed (blocking)
                synchronized (this.notifyObj) {
                    this.notifyObj.wait();
                }
            }

            // If successful, return value if we have it, or else what the task has
            if (this.task.isSuccessful()) {
                return this.value != null ? this.value : this.task.getResult();
            // If we got cancelled, throw interrupted exception
            } else if (this.cancelled || this.task.isCanceled()) {
                throw new InterruptedException();
            // If failed, throw the exception that made the task fail
            } else {
                throw new ExecutionException(
                        this.exception != null ? this.exception : this.task.getException()
                );
            }
        }

        @Override
        public T get(long timeout, @NonNull TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            if (!this.task.isComplete()) {
                synchronized (this.notifyObj) {
                    this.notifyObj.wait(unit.toMillis(timeout));
                }
            }

            if (this.task.isSuccessful()) {
                return this.value != null ? this.value : this.task.getResult();
            } else if (this.cancelled || this.task.isCanceled()) {
                throw new InterruptedException();
            } else if (this.task.isComplete() && !this.task.isSuccessful()) {
                throw new ExecutionException(this.exception != null ? this.exception
                        : this.task.getException());
            } else {
                throw new TimeoutException();
            }
        }
    }

    private static abstract class FutureTransformer<T, U> implements Future<U> {

        private Future<T> source;

        public FutureTransformer(Future<T> source) {
            this.source = source;
        }

        protected abstract U transform(T obj) throws Exception;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return source.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return source.isCancelled();
        }

        @Override
        public boolean isDone() {
            return source.isDone();
        }

        @Override
        public U get() throws InterruptedException, ExecutionException {
            try {
                return transform(source.get());
            } catch (InterruptedException | ExecutionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }

        @Override
        public U get(long timeout, @NonNull TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            try {
                return transform(source.get());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw e;
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }
    }
}
