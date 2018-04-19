package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class FilePickerViewModel extends ViewModel {

    /** Live event for having picked a file in the view, signalling that the activity should exit */
    private SingleLiveEvent<String> filePickEvent = new SingleLiveEvent<>();

    /** List of containers to display in the view */
    private List<Container> containers = new ArrayList<>();

    public List<Container> getContainers() {
        return containers;
    }

    public LiveData<String> getFilePickEvent() {
        return filePickEvent;
    }

    /**
     * Handles clicking a container in the view
     * @param container Container that was clicked
     */
    public void onContainerClick(Container container) {
        // Signal to the activity that we have picked a file
        this.filePickEvent.postValue(container.name);
    }

    /**
     * Regenerates the list of containers from files in the given directory, with the given
     * file extension.
     *
     * @param filesDir Directory of files to generate a list of containers from
     * @param extension Extension of files to include; null if any extension
     */
    public void generateContainers(File filesDir, @Nullable final String extension) {
        // Get files in our internal storage directory, and filter by extension (if not null).
        String[] fileNames = filesDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile()
                        && (extension == null || name.endsWith("." + extension));
            }
        });

        // Add each container to the list of containers
        this.containers.clear();
        for (String fileName : fileNames) {
            File file = new File(filesDir, fileName);
            this.containers.add(new Container(
                    fileName,
                    file.lastModified()
            ));
        }
    }

    public static class Container {

        private static final DateFormat format = DateFormat.getDateTimeInstance();
        public String name;
        public Date updated;

        public Container(String name, long updated) {
            this.name = name;
            this.updated = new Date(updated);
        }

        public String formatUpdated() {
            return format.format(updated);
        }

    }

}
