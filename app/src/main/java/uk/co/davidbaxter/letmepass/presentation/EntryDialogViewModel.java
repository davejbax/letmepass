package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

/**
 * Viewmodel for entry dialogs, where users may edit or view password database entries
 * @see PasswordDatabaseEntry
 */
public class EntryDialogViewModel extends ViewModel {

    private static final DateFormat format = DateFormat.getDateTimeInstance();

    /**
     * Live boolean of whether entry can be edited or not
     */
    private MutableLiveData<Boolean> editable = new MutableLiveData<>();

    /**
     * Live event to signal to observer(s) to close the dialog
     */
    private SingleLiveEvent<Void> closeEvent = new SingleLiveEvent<>();

    /**
     * Live event to signal to observer(s) to save a container
     */
    private SingleLiveEvent<PasswordDatabaseEntryContainer> saveEvent = new SingleLiveEvent<>();

    /**
     * Live data of working entry -- this is one that is a deep copy of the actual entry held in the
     * container. When saving, it should be copied into the container's entry (field-by-field) to
     * update it with the changes. When discarding changes, it should be re-set to a fresh deep
     * copy of the container's entry.
     */
    private MutableLiveData<PasswordDatabaseEntry> workingEntry = new MutableLiveData<>();

    private MutableLiveData<CreationViewModel.DummyPasswordFlagsClass> passwordFlags =
            new MutableLiveData<>();

    /**
     * Container holding the original entry to be edited. The entry held in this container need not
     * correspond to the exact entry held in the model, but it should be noted that observers that
     * pass this container in the first place are responsible for updating the entry in the model
     * based on new container contents. See `saveEvent`
     */
    private final PasswordDatabaseEntryContainer container;

    /**
     * Whether we started off editing an entry or not. This will allow us to carry out the proper
     * 'cancel' action (i.e. closing if we didn't switch to editing from viewing).
     */
    private final boolean startedAsEditable;

    private EntryDialogViewModel(PasswordDatabaseEntryContainer container,
                                 boolean editable) {
        this.startedAsEditable = editable;
        this.editable.setValue(editable);
        this.container = container;
        this.workingEntry.setValue((PasswordDatabaseEntry) container.getEntry().clone());
        this.passwordFlags.setValue(new CreationViewModel.DummyPasswordFlagsClass());

        // Initialize password flags for the current password
        if (container.getEntry().getType().equals(PasswordEntry.TYPE))
            this.passwordFlags.getValue().recalculate(
                    ((PasswordEntry) container.getEntry()).password
            );
    }

    /**
     * Gets live data of whether the entry is 'editable' right now (i.e. fields can be changed in
     * the view)
     * @return Live boolean
     */
    public LiveData<Boolean> getEditable() {
        return this.editable;
    }

    /**
     * Gets an event that signals when the dialog should close
     * @return Close dialog live event
     */
    public LiveData<Void> getCloseEvent() {
        return this.closeEvent;
    }

    /**
     * Gets an event that signals when the container should be 'saved' (i.e. its contents/the entry
     * it contains has been updated: it should be saved in the model and the view that opened
     * the dialog should be updated with these changes)
     * @return Save container live event
     */
    public LiveData<PasswordDatabaseEntryContainer> getSaveEvent() {
        return this.saveEvent;
    }

    /**
     * Gets the current working entry as live data. This is an entry that can be modified with
     * changes. These changes will be committed when calling {@link #onSave()}.
     * @return Current working entry live data
     */
    public LiveData<PasswordDatabaseEntry> getWorkingEntry() {
        return this.workingEntry;
    }

    public LiveData<CreationViewModel.DummyPasswordFlagsClass> getPasswordFlags() {
        return this.passwordFlags;
    }

    /**
     * Gets the icon resource ID for the entry being edited
     * @return Icon drawable resource ID
     */
    public int getIconId() {
        return container.getIconId();
    }

    /**
     * Formats a date (in the form of a Unix timestamp) into a readable String, based on the user's
     * locale.
     *
     * @param dateMillis Unix timestamp to format
     * @return Formatted date string
     */
    public String formatDate(long dateMillis) {
        Date date = new Date(dateMillis);
        return format.format(date);
    }

    public void onPasswordChanged(CharSequence newPassword, int start, int before, int count) {
        // Retrieve or create flags
        CreationViewModel.DummyPasswordFlagsClass flags = passwordFlags.getValue();
        if (flags == null)
            flags = new CreationViewModel.DummyPasswordFlagsClass();

        // Get password and recalculate flags based on it
        flags.recalculate(newPassword.toString());

        // Update flags in view
        passwordFlags.postValue(flags);
    }

    /**
     * Handles a switch to 'edit' mode for the entry
     */
    public void onEdit() {
        this.editable.postValue(true);
    }

    /**
     * Handles saving the working entry to the original container, and returns to viewing mode
     * (i.e. not editable).
     */
    public void onSave() {
        // TODO: validate

        // Update the container's entry with our working entry, 'saving' our changes to it.
        this.container.getEntry().fromCopy(this.workingEntry.getValue());

        // Stop our working entry being editable so we don't change something in this time
        this.editable.postValue(false);

        // Delegate saving to observers: we do not have the means to save.
        this.saveEvent.postValue(this.container);
    }

    /**
     * Handles cancelling any changes to the entry, and restores the original entry to the working
     * entry (see {@link #getWorkingEntry()})
     */
    public void onCancel() {
        // If we started in edit mode, we don't want to return to viewing mode: if the user chose to
        // edit an entry, they should be returned to the screen from which they chose this, not to
        // viewing the entry. Similarly if they chose to edit a new entry.
        if (this.startedAsEditable) {
            this.closeEvent.postValue(null);
        // If cancelling edits, re-set working entry to saved entry
        } else {
            this.workingEntry.postValue((PasswordDatabaseEntry) this.container.getEntry().clone());
            this.editable.postValue(false);
        }
    }

    /**
     * Handles closing the dialog (should be called while in viewing mode/not editing; if editing,
     * cancel would be a more appropriate option so the user can return to viewing, or be returned
     * to their previous screen)
     */
    public void onClose() {
        // Close the dialog
        this.closeEvent.postValue(null);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final PasswordDatabaseEntryContainer container;
        private final boolean editable;

        public Factory(PasswordDatabaseEntryContainer container, boolean editable) {
            this.container = container;
            this.editable = editable;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!(modelClass.equals(EntryDialogViewModel.class)))
                throw new IllegalArgumentException(
                        "Class must be instance of EntryDialogViewModel"
                );

            return (T) new EntryDialogViewModel(container, editable);
        }

    }

}
