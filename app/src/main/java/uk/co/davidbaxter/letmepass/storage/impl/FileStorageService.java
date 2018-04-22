package uk.co.davidbaxter.letmepass.storage.impl;

import android.app.usage.StorageStats;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;

import uk.co.davidbaxter.letmepass.ui.FilePickerActivity;

/**
 * A service to create {@link FileDataStore} objects through various means.
 *
 * This service allows for the creation of {@link FileDataStore} objects through:
 * <ul>
 *     <li>A file picker activity (see {@link #getFilePickerIntent()})</li>
 *     <li>The name (<b>not</b> path) of an existing file in internal storage (see
 *     {@link #createStoreFromFilename(String)})</li>
 * </ul>
 * <p>
 * The files picked and created through this service will reside in internal storage, in the app's
 * data directory.
 */
public class FileStorageService {

    private Context context;

    /**
     * Creates a new FileStorageService from the given context
     * @param context Context to use; ideally should be an application context
     * @see Context#getApplicationContext()
     */
    public FileStorageService(Context context) {
        this.context = context;
    }

    /**
     * Gets an intent to launch a file picker. This file picker will allow users to choose from a
     * list of files stored in the application's internal storage directory, where the files have
     * the correct extension to be loaded as a database.
     * <p>
     * This intent returns a result; it should be processed in order to produce a
     * {@link FileDataStore} with the method {@link #onFilePickerResult(Intent)}.
     *
     * @return File picker intent
     */
    public Intent getFilePickerIntent() {
        Intent intent = new Intent(context, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.EXTRA_EXTENSION, StorageConstants.EXTENSION);
        return intent;
    }

    /**
     * Processes the result of a file picker intent launched using an intent created with
     * {@link #getFilePickerIntent()}. This will produce a {@link FileDataStore} that may be used
     * to access the chosen file.
     *
     * @param data Data returned by the file picker activity
     * @return A FileDataStore that may be used to access the picked file
     * @see android.app.Activity#onActivityResult(int, int, Intent)
     */
    public FileDataStore onFilePickerResult(Intent data) {
        String path = data.getStringExtra(FilePickerActivity.EXTRA_PATH);
        if (path == null)
            throw new IllegalStateException("Intent data from file picker result is invalid");

        File file = new File(path);
        return new FileDataStore(file);
    }

    /**
     * Checks whether a file already exists in our internal storage with the file name `fileName`.
     *
     * @param fileName Name of file to check, without extension or path
     * @return True if the file exists in internal storage already
     */
    public boolean fileExists(String fileName) {
        return new File(context.getFilesDir(), getFullFileName(fileName)).exists();
    }

    /**
     * Checks whether a given filename to be used with other methods in the service is invalid.
     * <p>
     * An invalid filename is a name that, when combined with the internal storage directory, does
     * not result in a file that is:
     * <ul>
     *     <li>A child of our internal storage directory</li>
     *     <li>An existing directory</li>
     *     <li>A file with any extension already (should not have extension)</li>
     * </ul>
     *
     * @param fileName Filename to check
     * @return True if the filename is valid
     */
    public boolean isFileNameValid(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.getParentFile().equals(context.getFilesDir())
                || file.getName().isEmpty()
                || (file.exists() && !file.isFile())
                || file.getName().contains("."))
            return false;
        return true;
    }

    /**
     * Creates a {@link FileDataStore} from a given filename. This filename should <b>not</b>
     * include the extension, as this will be appended to it.
     * <p>
     * This method will work for files that already exist. It is advised to check whether the file
     * by the given fileName exists in our internal storage already by calling
     * {@link #fileExists(String)} first.
     *
     * @param fileName Name of file to create store from, without extension or path.
     * @return FileDataStore for the given file name
     * @throws IOException If the file could not be created to create a store from
     */
    public FileDataStore createStoreFromFilename(String fileName) throws IOException {
        File file = new File(context.getFilesDir(), getFullFileName(fileName));
        if (file.exists())
            file.createNewFile();

        return new FileDataStore(file);
    }

    private String getFullFileName(String fileName) {
        return fileName + "." + StorageConstants.EXTENSION;
    }

    // TODO:
    // - Loading from SharedPrefs
    // - Loading from anywhere (for launching app via view intent)

}
