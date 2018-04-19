package uk.co.davidbaxter.letmepass.storage.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.util.InstantFuture;

public class FileDataStore implements DataStore {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final File file;
    private byte[] cachedData = null;

    public FileDataStore(File file) {
        this.file = file;
    }

    @Nullable
    @Override
    public byte[] getCachedData() {
        return cachedData;
    }

    @Nullable
    @Override
    public String getCachedStoreName() {
        return file.getName();
    }

    @Override
    public Future<String> getStoreName() {
        return new InstantFuture<>(file.getName());
    }

    @Override
    public Future<byte[]> readData() {
        return executorService.submit(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                // Read the file input stream into a byte array
                FileInputStream fileIn = new FileInputStream(file);
                byte[] result = IOUtils.toByteArray(fileIn);

                // Update cached data
                cachedData = result;
                return result;
            }
        });
    }

    @Override
    public Future<Void> writeData(@NonNull final byte[] data) {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Copy the input to the file, overwriting contents
                ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                FileOutputStream fileOut = new FileOutputStream(file);
                IOUtils.copyStream(bytesIn, fileOut);

                // Update cached data
                cachedData = data;
                return null;
            }
        });
    }

    @Override
    public Future<Void> deleteStore() {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Delete the file and wipe our cached data
                file.delete();
                cachedData = null;
                return null;
            }
        });
    }
}
